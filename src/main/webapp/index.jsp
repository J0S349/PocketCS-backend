<html>
<script src="https://code.jquery.com/jquery-3.1.0.js"></script>
<body>
<h2>Hello World!</h2>

<script>
function getTable(){
  var value = "DataStructures";
  var tableName = "tableName";
  $.get("/rest/getTable",
    {tableName: value},
    function(result){
      result = decodeURI(result);
      result = decodeURI(result);
      console.log("result: " + result);
    })
    .fail(function(jqXHR, textStatus, errorThrown){
      console.log(errorThrown);
      console.log("Error");
    });
};
$(function(){
    getTable();
    console.log("Rest");
});

</script>

</body>
</html>
