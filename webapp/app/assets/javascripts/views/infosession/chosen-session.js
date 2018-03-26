import React, { Component } from 'react';


const ChosenSession = ({ attending }) =>
<div className="panel panel-default col-lg-12" style={{border:'none', padding:'0', margin: '0px'}}>
  <div className="panel-heading">Gekozen infosessies</div>
  <div class="col-lg-4">
    <dl class="dl-horizontal">
      <dt><strong>Wanneer?</strong></dt><dd>DEV! Waar is dit naar toe? </dd>
      <dt><strong>Waar?</strong></dt><dd>{attending.address.street + ' ' + attending.address.num + ', ' + attending.address.zip + ' ' + attending.address.city + '(' + attending.address.country + ')'}</dd>
      <dt><strong>Gastvrouw/-heer</strong></dt><dd>{attending.hostName}</dd>
      <dt><strong>Type</strong></dt><dd>{attending.type == "OTHER" ? "Andere (zie opmerkingen)" : attending.type}</dd>
      <dt><strong>Aantal deeln.</strong></dt><dd>{attending.enrolleeCount + '/' + attending.maxEnrollees}</dd>
      <dt><strong>Opmerkingen</strong></dt><dd>{attending.comments}</dd>
      <dt><strong>Beschrijving</strong></dt><dd>{attending.description}</dd>
    </dl>
  </div>
  <div class="col-lg-8">
      <div class="well map">
          <div id="map" />
      </div>
  </div>
</div>

export default ChosenSession
