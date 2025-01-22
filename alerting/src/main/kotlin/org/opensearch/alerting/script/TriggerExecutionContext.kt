/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.script

import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.MonitorRunResult
import org.opensearch.commons.alerting.model.Trigger
import java.time.Instant
import java.time.ZoneId

abstract class TriggerExecutionContext(
    open val monitor: Monitor,
    open val results: List<Map<String, Any>>,
    open val periodStart: Instant,
    open val periodEnd: Instant,
    open val error: Exception? = null
) {

    constructor(monitor: Monitor, trigger: Trigger, monitorRunResult: MonitorRunResult<*>) :
        this(
            monitor, monitorRunResult.inputResults.results, monitorRunResult.periodStart,
            monitorRunResult.periodEnd, monitorRunResult.scriptContextError(trigger)
        )

    /**
     * Mustache templates need special permissions to reflectively introspect field names. To avoid doing this we
     * translate the context to a Map of Strings to primitive types, which can be accessed without reflection.
     */
    open fun asTemplateArg(): Map<String, Any?> {
        // Convert periodStart and periodEnd to specified timezone
        val timezone = monitor.triggers.firstOrNull()?.actions?.firstOrNull()?.timezone
        val periodStartWithTz = if (!timezone.isNullOrEmpty()) {
            periodStart.atZone(ZoneId.of(timezone)).toString().replace("[", " [")
        } else {
            periodStart
        }
        val periodEndWithTz = if (!timezone.isNullOrEmpty()) {
            periodEnd.atZone(ZoneId.of(timezone)).toString().replace("[", " [")
        } else {
            periodEnd
        }
        return mapOf(
            "monitor" to monitor.asTemplateArg(),
            "results" to results,
            "periodStart" to periodStartWithTz,
            "periodEnd" to periodEndWithTz,
            "error" to error
        )
    }
}
