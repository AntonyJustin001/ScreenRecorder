package tena.health.care.models

data class User(
    val userId: String = "",
    val name: String = "",
    val password: String = "",
    val emailId: String = "",
    val mobileNo: String = "",
    val whatsapp: String = "",
    val DOB: String = "",
    val address: String = "",
    val instaId: String = "",
    val pointsEarned: String = "",
    val offersAvailable: String = ""
)