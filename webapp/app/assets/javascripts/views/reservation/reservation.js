import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReservationFastTrack from './fast-track';
import Datepicker from './reservation-datepicker';
import {extraInfoSwitch, extraInfoChange, createReservation, switchReserveBtn} from './../../actions/car';
import {Popover, OverlayTrigger, Button} from 'react-bootstrap';
import Snackbar from '../Snackbar/Snackbar'

const Reservation = ({onExtraInfoSwitch, suggestionText, extraInfoState, onExtraInfoChange, extraInfoText, onCreateReservation, errormessage, isDisabled, startDate, endDate, suggestion, onSwitchReserveBtn}) => {
    return(
        <div style={{cursor: 'pointer' }}>
            <div className="row">
                <div className="col-sm-12">
                    <ReservationFastTrack />
                </div>
            </div>
            {
                suggestion == undefined ? '' :             
                <div className="row" style={{marginTop: '10px'}}>
                    <div className="col-sm-12">
                        <Datepicker />  
                        <OverlayTrigger trigger="click" placement="bottom" overlay={popoverBottom(onExtraInfoChange, extraInfoText)}>
                            <Button style={{marginLeft: '5px'}}onClick={SwitchExtraInfoState(onExtraInfoSwitch, extraInfoState)} type="button" className='btn btn-default'>Voorstel voor sleutelafspraak, vraag of opmerking</Button>
                        </OverlayTrigger> 
                    </div>
                </div>
            }
            {
                suggestion == undefined ? '' :             
                <div className="row" style={{marginTop: '10px'}}>
                    <div className="col-sm-12">
                        <Button onClick={ReserveerClicked(onCreateReservation)}
                        type="button"
                        className='btn btn-primary btn-lg'
                        disabled={startDate == null || endDate == null ? isDisabled || suggestion == undefined : !isDisabled}>
                            Reserveer
                        </Button>
                    </div>
                </div>
            }
            {
                errormessage != '' ?
                    <div className="row" style={{marginTop: '10px'}}>
                        <div className="col-sm-12">
                            <div className="alert alert-danger" role="alert">
                                <p>{errormessage}</p>
                            </div>  
                        </div>      
                    </div>
                : ''
        }
                <Snackbar />
        </div>
    )
}

const popoverBottom = (onExtraInfoChange, extraInfoText) => (
    <Popover
        id="popover-positioned-bottom"
        title="Voorstel voor sleutelafspraak, vraag of opmerking (wordt naar de eigenaar gemaild)"
    >
        <textarea value={extraInfoText} onChange={ExtraInfoChanged(onExtraInfoChange)} rows="5" className="form-control input-md"></textarea>
    </Popover>
);

const ReserveerClicked = (onCreateReservation) => () => {
    return onCreateReservation()
}

const SwitchExtraInfoState = (onExtraInfoSwitch, extraInfoState) => () => {
    return onExtraInfoSwitch(extraInfoState)
}

const ExtraInfoChanged = (onExtraInfoChange) => (event) => {
    var temp = event
    return onExtraInfoChange(temp.target.value)
}

const mapStateToProps = (state) => {
    return{
        extraInfoState: state.cars.reservation.extraInfoState,
        extraInfoText: state.cars.reservation.extraInfoText,
        errormessage: state.cars.view.err,
        startDate: state.cars.reservation.datepicker.startDate,
        endDate: state.cars.reservation.datepicker.endDate,
        suggestion: state.cars.view.suggestion,
        isDisabled: state.cars.view.isDisabled
    }}

const mapDispatchToProps = {
    onExtraInfoSwitch: extraInfoSwitch,
    onExtraInfoChange: extraInfoChange,
    onCreateReservation: createReservation
}

const reservationContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Reservation)

export default reservationContainer
