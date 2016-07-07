function validateForm() {
    var b = true;

    var x = document.forms["ip"]["ipServer"].value;
    var result = document.getElementById("result");
    if (x == null || x == "" || isNaN(x) ) {
        result.innerHTML = "Не корректные данные!!!";
        b = false;
    }

    return b;
}
