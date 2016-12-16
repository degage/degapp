import * as mysql from 'mysql'
import rp from 'request-promise'

console.log(new Date(), 'Start update gps coordinates ...')

var connection = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: 'mysql',
  database: 'degage'
})

connection.connect(function(err) {
  if (err) {
    console.error('error connecting: ' + err.stack);
    return;
  }

  console.log(new Date(), 'connected as id ', connection.threadId);
});

const getGeoLocation = (address) => {
  var options = {
      uri: 'https://maps.googleapis.com/maps/api/geocode/json',
      qs: {
          key: 'AIzaSyD1rykJueTRpT3slqiovYfm-YESfq0sh4U', // -> uri + '?access_token=xxxxx%20xxxxx'
          address: address
      },
      headers: {
          'User-Agent': 'Request-Promise'
      },
      json: true // Automatically parses the JSON string in the response
  };
    return rp(options)
}

const updateGpsCoordinates = (id, lat, lng) => {
  const query = 'update addresses set address_latitude = ?, address_longitude = ? where address_id = ?'
  return new Promise( (resolve, reject) => {
    connection.query(query, [lat, lng, id], function(err, results) {
      if (err) {
        reject(err)
      } else {
        resolve(results)
      }
    })
  })
}

const query = "SELECT address_id, concat(address_street, ' ', address_number, ', ', \
  address_zipcode, ' ', address_city, ', ', address_country) as address, address_latitude, \
  address_longitude from addresses where address_latitude is null and address_street != ''"

connection.query(query, function(err, rows, fields) {
  if (err) throw err;
  rows.forEach( row => {
    console.log(row.address_id, row.address)
    getGeoLocation(row.address)
    .then(function (result) {
        if (result !== undefined && result.results[0] !== undefined &&
          result.results[0].geometry !== undefined && result.results[0].geometry.location !== undefined) {
            const location = result.results[0].geometry.location
            console.log('location', location)
            updateGpsCoordinates(row.address_id, location.lat, location.lng).then(
              x => {console.log('ok', x)}
            , err => {
              console.error('Err!', err)
            })
          } else {
            console.log('bad result', result)
          }
    }, function(err) {
        console.error('err', err)
      })
  })
});
