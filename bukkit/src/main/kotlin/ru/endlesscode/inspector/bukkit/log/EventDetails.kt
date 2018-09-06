package ru.endlesscode.inspector.bukkit.log

import org.bukkit.event.Event
import ru.endlesscode.inspector.bukkit.util.EventsUtils
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal object EventDetails {

    private val detailsGetters: Map<String, DetailsGetter<out Event>> = EventsUtils.eventsClasses.map { eventClass ->
        eventClass.simpleName to DetailsGetter(eventClass)
    }.toMap()

    /**
     * Returns details for the given [event].
     */
    fun <E : Event> forEvent(event: E, eventClass: Class<in E> = event.javaClass): Pair<String, List<String>> {
        if (eventClass == Event::class.java) return "Event" to mutableListOf()

        val (parentHierarchy, parentDetails) = forEvent(event, eventClass.superclass)
        val details = detailsGetters[eventClass.simpleName]?.getDetails(event) ?: emptyList()

        return "$parentHierarchy < ${eventClass.simpleName}" to (parentDetails + details)
    }


    private class DetailsGetter<EventT : Event>(eventClass: Class<EventT>) {

        private var fieldsMap: Map<String, Field>

        init {
            fieldsMap = eventClass.declaredFields
                    .filter { !Modifier.isStatic(it.modifiers) }
                    .map { field ->
                        field.isAccessible = true
                        field.name to field
                    }.toMap()
        }

        fun getDetails(event: Event): List<String> {
            return try {
                fieldsMap.map { (name, field) -> "$name: ${field.get(event)}" }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}