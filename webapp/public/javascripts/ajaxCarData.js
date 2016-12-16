function loadCarData ( carId, dateString  ) {
  console.log('date1', carId, dateString);
   $.ajax(myJsRoutes.controllers.Calendars.availabilityCar(carId, dateString))
   .done (function (html) {
       $ ("#car-availability-panel").html (html) ;
   })
   .fail (function ( ) {
       // TODO: make clearer
       $ ( "#car-availability-panel" ).html ("Er ging iets mis...") ;
   });
}