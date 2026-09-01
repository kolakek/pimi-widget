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
    PIMI(
        "weather_app_default",
        "",
        R.string.config_weather_app_pimi
    ),
    ACCU(
        "weather_app_accu",
        "com.accuweather.android",
        R.string.config_weather_app_accu
    ),
    AEMET(
        "weather_app_aemet",
        "es.aemet",
        R.string.config_weather_app_aemet
    ),
    ASUS(
        "weather_app_asus",
        "com.asus.weathertime",
        R.string.config_weather_app_asus
    ),
    ASUS2(
        "weather_app_asus2",
        "com.asus.rog.rogweather",
        R.string.config_weather_app_asus
    ),
    BOM(
        "weather_app_bom",
        "au.gov.bom.metview",
        R.string.config_weather_app_bom
    ),
    BREEZY(
        "weather_app_breezy",
        "org.breezyweather",
        R.string.config_weather_app_breezy
    ),
    BURA(
        "weather_app_bura",
        "com.davidtakac.bura",
        R.string.config_weather_app_bura
    ),
    CAN(
        "weather_app_can",
        "ca.gc.ec.weather_app_android.ops",
        R.string.config_weather_app_can
    ),
    DMI(
        "weather_app_dmi",
        "dk.dmi.byvejret",
        R.string.config_weather_app_dmi
    ),
    DWD(
        "weather_app_dwd",
        "de.dwd.warnapp",
        R.string.config_weather_app_dwd
    ),
    KNMI(
        "weather_app_knmi",
        "nl.knmi.weer",
        R.string.config_weather_app_knmi
    ),
    MAWEATHER(
        "weather_app_maweather",
        "com.vayunmathur.weather",
        R.string.config_weather_app_maweather
    ),
    METEOFR(
        "weather_app_meteofr",
        "fr.meteo",
        R.string.config_weather_app_meteofr
    ),
    METEOSWISS(
        "weather_app_meteoswiss",
        "ch.admin.meteoswiss",
        R.string.config_weather_app_meteoswiss
    ),
    METOFF(
        "weather_app_metoffice",
        "uk.gov.metoffice.weather.android",
        R.string.config_weather_app_metoff
    ),
    MSN(
        "weather_app_msn",
        "com.microsoft.amp.apps.bingweather",
        R.string.config_weather_app_msn
    ),
    OPENW(
        "weather_app_openw",
        "uk.co.openweather",
        R.string.config_weather_app_openw
    ),
    OPPO(
        "weather_app_oppo",
        "com.coloros.weather2",
        R.string.config_weather_app_oppo
    ),
    OVERMORROW(
        "weather_app_overmorrow",
        "com.marotidev.Overmorrow",
        R.string.config_weather_app_overmorrow
    ),
    PIXEL(
        "weather_app_pixel",
        "com.google.android.apps.weather",
        R.string.config_weather_app_pixel
    ),
    SAMS(
        "weather_app_sams",
        "com.sec.android.daemonapp",
        R.string.config_weather_app_sams
    ),
    WMASTER(
        "weather_app_wmaster",
        "com.pranshulgg.weather_master_app",
        R.string.config_weather_app_wmaster
    ),
    NEFFOS(
        "weather_app_neffos",
        "com.tplink.weather",
        R.string.config_weather_app_neffos
    ),
    XIAOMI(
        "weather_app_xiaomi",
        "com.miui.weather2",
        R.string.config_weather_app_xiaomi
    ),
    YAHOO(
        "weather_app_yahoo",
        "com.yahoo.mobile.client.android.weather",
        R.string.config_weather_app_yahoo
    ),
    YR(
        "weather_app_yr",
        "no.nrk.yr",
        R.string.config_weather_app_yr
    )
}
