/*
 * This file is part of Pimi Widget.
 *
 * Pimi Widget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.kolakek.pimiwidget.utility

import com.kolakek.pimiwidget.R

enum class WeatherApp(val key: String, val packageName: String, val labelId: Int) {
    NONE(
        "weather_app_default",
        "",
        R.string.config_weather_app_default
    ),
    BREEZY(
        "weather_app_breezy",
        "org.breezyweather",
        R.string.config_weather_app_breezy
    ),
    METEOFR(
        "weather_app_meteofr",
        "fr.meteo",
        R.string.config_weather_app_meteofr
    ),
    KNMI(
        "weather_app_knmi",
        "nl.knmi.weer",
        R.string.config_weather_app_knmi
    ),
    DWD(
        "weather_app_dwd",
        "de.dwd.warnapp",
        R.string.config_weather_app_dwd
    ),
    METOFF(
        "weather_app_metoffice",
        "uk.gov.metoffice.weather.android",
        R.string.config_weather_app_metoff
    ),
    AEMET(
        "weather_app_aemet",
        "es.aemet",
        R.string.config_weather_app_aemet
    ),
    METEOSWISS(
        "weather_app_meteoswiss",
        "ch.admin.meteoswiss",
        R.string.config_weather_app_meteoswiss
    ),
    YR(
        "weather_app_yr",
        "no.nrk.yr",
        R.string.config_weather_app_yr
    ),
    BOM(
        "weather_app_bom",
        "au.gov.bom.metview",
        R.string.config_weather_app_bom
    ),
    CAN(
        "weather_app_can",
        "ca.gc.ec.weather_app_android.ops",
        R.string.config_weather_app_can
    ),
    DMI(
        "weather_app_dmi",
        "dk.dmi.byvejret",
        R.string.config_weather_dmi
    ),
    PIXEL(
        "weather_app_pixel",
        "com.google.android.apps.weather",
        R.string.config_weather_app_pixel
    )
}
