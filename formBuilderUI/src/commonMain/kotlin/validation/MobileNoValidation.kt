package validation

fun checkMobileNoValidation(mobileNo: String): String {
    if (mobileNo.isBlank()) return "Mobile number cannot be empty."

    // Check if the first digit is valid
    if (mobileNo.first().digitToInt() <= 5)
        return "Mobile number must start with a digit greater than 5."

    // Check if the length is valid (e.g., assuming a 10-digit number)
    if (mobileNo.length != 10) return "Mobile number must be 10 digits long."

    // Check if all characters are digits
    if (!mobileNo.all { it.isDigit() }) return "Mobile number must contain only digits."

    // If all checks pass
    return ""
}