package validation

fun checkMobileNoValidation(mobileNo: String): String {

    if (mobileNo.isNotBlank() && mobileNo.first().digitToInt() <= 5)
        return "The mobile number must start with a digit greater than 5"

    return ""
}