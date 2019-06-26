package com.example.myweather.network

data class WeatherCity(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<WeatherDay>,
    val message: Double
) {
    data class City(
        val coord: Coord,
        val country: String,
        val id: Int,
        val name: String,
        val population: Int
    ) {

        data class Coord(
            val lat: Double,
            val lon: Double
        )
    }

    data class WeatherDay(
        val clouds: Int,
        val deg: Int,
        val dt: Double,
        val humidity: Int,
        val pressure: Double,
        val speed: Double,
        val temp: Temp,
        val weather: List<Weather>
    ) {
        data class Weather(
            val description: String,
            val icon: String,
            val id: Int,
            val main: String
        )

        data class Temp(
            val day: Double,
            val eve: Double,
            val max: Double,
            val min: Double,
            val morn: Double,
            val night: Double
        )
    }

}





