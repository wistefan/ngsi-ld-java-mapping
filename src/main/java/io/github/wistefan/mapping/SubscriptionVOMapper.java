package io.github.wistefan.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wistefan.mapping.annotations.AttributeSetter;
import io.github.wistefan.mapping.annotations.MappingEnabled;
import lombok.extern.slf4j.Slf4j;
import org.fiware.ngsi.model.*;
import reactor.core.publisher.Mono;

import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Mapper to handle translation from NGSI-LD subscriptions to Java-Objects, based on annotations added to the target class
 */
@Slf4j
@Singleton
public class SubscriptionVOMapper extends Mapper {

    private final ObjectMapper objectMapper;

    public SubscriptionVOMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Method to map an NGSI-LD Subscription into a Java-Object of class targetClass. The class has to provide a
     * string constructor to receive the subscription id
     *
     * @param subscriptionVO    the NGSI-LD subscription to be mapped
     * @param targetClass class of the target object
     * @param <T>         generic type of the target object, has to extend provide a string-constructor to receive the
     *           subscription id
     * @return the mapped object
     */
    public <T> Mono<T> fromSubscriptionVO(SubscriptionVO subscriptionVO, Class<T> targetClass) {

        Optional<MappingEnabled> optionalMappingEnabled = isMappingEnabled(targetClass);
        if (optionalMappingEnabled.isEmpty()) {
            return Mono.error(new MappingException(String.format("Mapping is not enabled for class %s", targetClass)));
        }

        MappingEnabled mappingEnabled = optionalMappingEnabled.get();

        if (!Arrays.stream(mappingEnabled.subscriptionType()).toList().contains(subscriptionVO.getType().getValue())) {
            return Mono.error(new MappingException(String.format("Subscription and Class type do not match - %s vs %s.",
                    subscriptionVO.getType().getValue(), Arrays.asList(mappingEnabled.subscriptionType()))));
        }

        try {
            Constructor<T> objectConstructor = targetClass.getDeclaredConstructor(String.class);
            T constructedObject = objectConstructor.newInstance(subscriptionVO.getId().toString());

            // handle "well-known" properties
            Map<String, Object> propertiesMap = new LinkedHashMap<>();
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_NAME, propertyVOFromValue(subscriptionVO.getName()));
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_CREATED_AT, propertyVOFromValue(subscriptionVO.getCreatedAt()));
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_MODIFIED_AT, propertyVOFromValue(subscriptionVO.getModifiedAt()));
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_EXPIRES, propertyVOFromValue(subscriptionVO.getExpires()));
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_Q, propertyVOFromValue(subscriptionVO.getQ()));
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_GEO_Q, subscriptionVO.getGeoQ());
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_ENTITIES, subscriptionVO.getEntities());
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_WATCHED_ATTRIBUTES, subscriptionVO.getWatchedAttributes());
            propertiesMap.put(SubscriptionVO.JSON_PROPERTY_NOTIFICATION, subscriptionVO.getNotification());

            List<Mono<T>> singleInvocations = propertiesMap.entrySet().stream()
                    .map(entry -> getObjectInvocation(entry, constructedObject, subscriptionVO.getId().toString()))
                    .toList();

            return Mono.zip(singleInvocations, constructedObjects -> constructedObject);

        } catch (NoSuchMethodException e) {
            return Mono.error(new MappingException(String.format("The class %s does not declare the required String id constructor.", targetClass)));
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            return Mono.error(new MappingException(String.format("Was not able to create instance of %s.", targetClass), e));
        }
    }

    /**
     * Helper method to create a propertyVO for well-known(thus flat) properties
     *
     * @param value the value to wrap
     * @return a propertyVO containing the value
     */
    private PropertyVO propertyVOFromValue(Object value) {
        PropertyVO propertyVO = new PropertyVO();
        propertyVO.setValue(value);
        return propertyVO;
    }

    /**
     * Get the invocation on the object to be constructed.
     *
     * @param entry                   additional properties entry
     * @param objectUnderConstruction the new object, to be filled with the values
     * @param subscriptionId          id of the subscription
     * @param <T>                     class of the constructed object
     * @return                        single, emitting the constructed object
     */
    private <T> Mono<T> getObjectInvocation(Map.Entry<String, Object> entry, T objectUnderConstruction,
                                            String subscriptionId) {
        Optional<Method> optionalSetter = getCorrespondingSetterMethod(objectUnderConstruction, entry.getKey());
        if (optionalSetter.isEmpty()) {
            log.warn("Ignoring property {} for subscription {} since there is no mapping configured.", entry.getKey(), subscriptionId);
            return Mono.just(objectUnderConstruction);
        }
        Method setterMethod = optionalSetter.get();
        Optional<AttributeSetter> optionalAttributeSetter = getAttributeSetterAnnotation(setterMethod);
        if (optionalAttributeSetter.isEmpty()) {
            log.warn("Ignoring property {} for subscription {} since there is no attribute setter configured.", entry.getKey(), subscriptionId);
            return Mono.just(objectUnderConstruction);
        }
        AttributeSetter setterAnnotation = optionalAttributeSetter.get();

        Class<?> parameterType = getParameterType(setterMethod.getParameterTypes());

        return switch (setterAnnotation.value()) {
            case PROPERTY -> handleProperty((PropertyVO) entry.getValue(), objectUnderConstruction, optionalSetter.get(), parameterType);
            case GEO_QUERY -> handleGeoQuery((GeoQueryVO) entry.getValue(), objectUnderConstruction, optionalSetter.get(), parameterType);
            case ENTITY_INFO_LIST -> handleEntityInfoList((List<EntityInfoVO>) entry.getValue(), objectUnderConstruction,
                    optionalSetter.get(), setterAnnotation);
            case PROPERTY_SET -> handlePropertySet((Set<Object>) entry.getValue(), objectUnderConstruction, optionalSetter.get(), parameterType);
            case NOTIFICATION_PARAMS -> handleNotificationParams((NotificationParamsVO) entry.getValue(), objectUnderConstruction, optionalSetter.get(), parameterType);
            default -> Mono.error(new MappingException(String.format("Received type %s is not supported.", setterAnnotation.value())));
        };
    }

    /**
     * Handle the evaluation of a property entry. Returns a single, emitting the target object, while invoking the property setting method.
     *
     * @param propertyVO              the value of the property
     * @param objectUnderConstruction the object under construction
     * @param setter                  the setter to be used for the property
     * @param parameterType           type of the property in the target object
     * @param <T>                     class of the object under construction
     * @return the single, emitting the objectUnderConstruction
     */
    private <T> Mono<T> handleProperty(PropertyVO propertyVO, T objectUnderConstruction, Method setter, Class<?> parameterType) {
        return invokeWithExceptionHandling(setter, objectUnderConstruction, objectMapper.convertValue(propertyVO.getValue(), parameterType));
    }

    /**
     * Handle the evaluation of a property entry. Returns a single, emitting the target object, while invoking the property setting method.
     *
     * @param propertySet             the value of the property
     * @param objectUnderConstruction the object under construction
     * @param setter                  the setter to be used for the property
     * @param parameterType           type of the property in the target object
     * @param <T>                     class of the object under construction
     * @return the single, emitting the objectUnderConstruction
     */
    private <T> Mono<T> handlePropertySet(Set<Object> propertySet, T objectUnderConstruction, Method setter, Class<?> parameterType) {
        return invokeWithExceptionHandling(setter, objectUnderConstruction, objectMapper.convertValue(propertySet, parameterType));
    }

    /**
     * Handle the evaluation of an entity info list entry. Returns a single, emitting the target object, while invoking
     * the property setting method.
     *
     * @param entityInfoVOS           the list containing the entity infos
     * @param objectUnderConstruction the object under construction
     * @param setter                  the setter to be used for the property
     * @param <T>                     class of the object under construction
     * @return the single, emitting the objectUnderConstruction
     */
    private <T> Mono<T> handleEntityInfoList(List<EntityInfoVO> entityInfoVOS, T objectUnderConstruction, Method setter,
                                             AttributeSetter setterAnnotation) {
        return invokeWithExceptionHandling(setter, objectUnderConstruction, entityInfoVOS != null ?
                entityInfoVOS.stream().map(entityInfoVO ->
                objectMapper.convertValue(entityInfoVO, setterAnnotation.targetClass())).toList() : null);
    }

    /**
     * Handle the evaluation of a geo query entry. Returns a single, emitting the target object, while invoking the geo
     * query setting method.
     *
     * @param geoQueryVO              the value of the geo query
     * @param objectUnderConstruction the object under construction
     * @param setter                  the setter to be used for the property
     * @param <T>                     class of the object under construction
     * @return the single, emitting the objectUnderConstruction
     */
    private <T> Mono<T> handleGeoQuery(GeoQueryVO geoQueryVO, T objectUnderConstruction, Method setter, Class<?> parameterType) {
        return invokeWithExceptionHandling(setter, objectUnderConstruction, objectMapper.convertValue(geoQueryVO, parameterType));
    }

    /**
     * Handle the evaluation of a geo query entry. Returns a single, emitting the target object, while invoking the geo
     * query setting method.
     *
     * @param notificationParamsVO    the value of the geo query
     * @param objectUnderConstruction the object under construction
     * @param setter                  the setter to be used for the property
     * @param <T>                     class of the object under construction
     * @return the single, emitting the objectUnderConstruction
     */
    private <T> Mono<T> handleNotificationParams(NotificationParamsVO notificationParamsVO, T objectUnderConstruction,
                                       Method setter, Class<?> parameterType) {
        return invokeWithExceptionHandling(setter, objectUnderConstruction, objectMapper.convertValue(notificationParamsVO, parameterType));
    }

    /**
     * Invoke the given method and handle potential exceptions.
     */
    private <T> Mono<T> invokeWithExceptionHandling(Method invocationMethod, T objectUnderConstruction, Object... invocationArgs) {
        try {
            invocationMethod.invoke(objectUnderConstruction, invocationArgs);
            return Mono.just(objectUnderConstruction);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            return Mono.error(new MappingException(String.format("Was not able to invoke method %s.", invocationMethod.getName()), e));
        }
    }

    /**
     * Return the type of the setter's parameter.
     */
    private Class<?> getParameterType(Class<?>[] arrayOfClasses) {
        if (arrayOfClasses.length != 1) {
            throw new MappingException("Setter method should only have one parameter declared.");
        }
        return arrayOfClasses[0];
    }

    /**
     * Get the setter method for the given property at the subscription.
     */
    private <T> Optional<Method> getCorrespondingSetterMethod(T subscription, String propertyName) {
        return getAttributeSettersMethods(subscription).stream().filter(m ->
                        getAttributeSetterAnnotation(m)
                                .map(attributeSetter -> attributeSetter.targetName().equals(propertyName)).orElse(false))
                .findFirst();
    }

    /**
     * Get all attribute setters for the given subscription
     */
    private <T> List<Method> getAttributeSettersMethods(T subscription) {
        return Arrays.stream(subscription.getClass().getMethods()).filter(m -> getAttributeSetterAnnotation(m)
                .isPresent()).toList();
    }

    /**
     * Get the attribute setter annotation from the given method, if it exists.
     */
    private Optional<AttributeSetter> getAttributeSetterAnnotation(Method m) {
        return Arrays.stream(m.getAnnotations()).filter(AttributeSetter.class::isInstance)
                .findFirst()
                .map(AttributeSetter.class::cast);
    }

}
