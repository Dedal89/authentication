
function logAsGuest(){

           var selectionMail = document.getElementById("email");
           selectionMail.value = "guest@gmail.com";

           var selectionPsw = document.getElementById("password");
           selectionPsw.value = "12345";

           document.getElementById('myLoginForm').submit();

}


function fillForm(){
        var n = Math.floor((Math.random()*5000)+1);
        var n2= Math.floor((Math.random()*5000)+1);

        n = n + n2;

        document.getElementById("name").value ="Guest";
        document.getElementById("email").value = n+"@gmail.com";
        document.getElementById("password").value = "12345";
        document.getElementById("RepeatPassword").value = "12345";
        document.getElementById("nation").value = "None";
        document.getElementById("accept_terms").checked=true;
}