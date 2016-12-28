<html>
<script src="https://code.jquery.com/jquery-3.1.0.js"></script>
<body>
<h2>Welcome to the Home page of the PocketCS-app</h2>

<script>
function getTable(){
  var value = "potato, beans, lettuce";
  var tableName = "items";
  $.get("/rest/getRecipes",
    {"items": value},
    function(result){

      //console.log("result: " + result);
      var val = JSON.parse(result);
      var recipes = val["hits"];
      for(var key in recipes){
        var recipeObject = recipes[key]["recipe"];
        console.log("recipe name: " + recipeObject["label"]);
        console.log("Image URL: " + recipeObject["image"]);
        console.log("Recipe URL: " + recipeObject["url"]);
        console.log("--------------------------------------------------");
      }


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
