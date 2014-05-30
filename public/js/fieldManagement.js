/**
 * Created by Riboni1989 on 30/05/14.
 */

$(document).ready(function(){

    $("#appCode").val($("#getCode").val());

    $("#fieldOfExpertise").change(function(){
        if($(this).val() == " -- other -- "){
            $("#otherFieldOfExpertise").attr('disabled' , false);
        }
        else{
            $("#otherFieldOfExpertise").attr('disabled' , true);
        }
    });
});
