function loadModal ( refuelId ) {
    myJsRoutes.controllers.Refuels.provideRefuelInfo ( refuelId ).ajax ( {
        success : function ( html ) {
            $ ( "#resultModal" ).html ( html ) ;
            $ ( '#detailsModal' ).modal ( 'show' ) ;
        },
        error : function ( ) {
            // TODO: make clearer
            $ ( "#resultModal" ).html ( "Er ging iets mis..." ) ;
        }
    } ) ;
}