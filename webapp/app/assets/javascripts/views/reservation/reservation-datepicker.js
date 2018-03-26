/**
 * Created by seppesnoeck on 29/01/18.
 */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import DatePicker from 'react-datepicker';
import moment from 'moment';
import {setCarStartDatepicker, setCarStopDatepicker} from "./../../actions/car"

const ReservationDatePicker = ({ startDate, endDate, onSetCarStartDatepicker, onSetCarStopDatepicker}) => {
    return(
        <div>
            <div style={{float: 'left'}}>
                <DatePicker
                    selected={startDate}
                    selectsStart
                    startDate={startDate}
                    endDate={endDate}
                    onChange={CarStartDatepickerChanged(onSetCarStartDatepicker, this, endDate)}
                    dateFormat="DD/MM/YYYY HH:mm"
                    showTimeSelect
                    timeFormat="HH:mm"
                    timeIntervals={15}
                    minDate={moment()}
                    placeholderText="Kies een startdatum"
                    className="reservation-db"
                />
            </div>
            <i className="glyphicon glyphicon-arrow-right" style={{float: 'left', padding: '8px 16px'}} ></i>
            <div style={{float: 'left'}}>
                <DatePicker
                    selected={endDate}
                    selectsEnd
                    startDate={startDate}
                    endDate={endDate}
                    onChange={CarStopDatepickerChanged(onSetCarStopDatepicker, this, startDate)}
                    dateFormat="DD/MM/YYYY HH:mm"
                    showTimeSelect
                    timeFormat="HH:mm"
                    timeIntervals={15}
                    minDate={moment()}
                    placeholderText="Kies een einddatum"
                    className="reservation-db"
                    style={{margin: '0px'}}
                />
            </div>
        
        </div>
    )

}

const CarStartDatepickerChanged = (onSetCarStartDatepicker) => (date, endDate) => {
    return onSetCarStartDatepicker(date, endDate)
}

const CarStopDatepickerChanged = (onSetCarStopDatepicker) => (date, startDate) => {
    return onSetCarStopDatepicker(date, startDate)
}

const mapStateToProps = (state) => {
    return{
        startDate: state.cars.reservation.datepicker.startDate,
        endDate: state.cars.reservation.datepicker.endDate
    }}

const mapDispatchToProps = {
    onSetCarStartDatepicker: setCarStartDatepicker,
    onSetCarStopDatepicker: setCarStopDatepicker
}

const ReservationDatePickerContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ReservationDatePicker)

export default ReservationDatePickerContainer
