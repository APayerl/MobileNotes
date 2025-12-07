package se.payerl.mobilenotes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform