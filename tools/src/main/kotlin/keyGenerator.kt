fun generateKey(length: Int): String = CharArray(length) { validChars.random() }.concatToString()
val validChars: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')