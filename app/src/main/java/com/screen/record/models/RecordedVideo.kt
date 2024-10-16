package tena.health.care.models

data class RecordedVideo(
    val videoName: String = "",
    val section: String = "",
    val subject: String = "",
    val date: String = "",
    val videoUrl: String = "",
    val teacherName : String = "",
    val status : String = "",
)