import React, { Component } from 'react';
import { Link } from 'react-router-dom'

const InfoSessionItem = ({ infosession }) =>
<tr>
  <td>
    <p>{infosession.type === "OWNER" ? "Enkel voor Auto-eigenaar" : infosession.type === "NORMAL" ? "Enkel voor Autolener" : infosession.description}</p>
  </td>
  <td>
    <p>Leeg tijdsobject</p>
  </td>
  <td>
    <p>{infosession.enrolleeCount + " / " + infosession.maxEnrollees}</p>
  </td>
  <td>
    <p>{infosession.hostName}</p>
  </td>
  <td>
    <p>{infosession.address.street + ', ' + infosession.address.num + ', ' + infosession.address.zip + ', ' + infosession.address.city + ' (' + infosession.address.country + ')'}</p>
  </td>
  <td>
    <Link onClick={() => this.setState()} to={"infosession/enroll?id=" + infosession.id} replace className='btn btn-link'>Inschrijven</Link>
  </td>
</tr>


export default InfoSessionItem
