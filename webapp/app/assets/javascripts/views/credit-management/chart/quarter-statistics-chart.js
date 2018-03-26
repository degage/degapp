import React, { Component } from 'react';
import {LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'

const QuarterStatisticsChart = ({quarterStatistics}) =>
<ResponsiveContainer width='80%' height={300}>
  <LineChart width={600} height={300} data={quarterStatistics.reverse()}
        margin={{top: 5, right: 30, left: 20, bottom: 5}}>
   <XAxis dataKey="billingDescription"/>
   <YAxis/>
   <CartesianGrid strokeDasharray="3 3"/>
   <Tooltip/>
   <Legend />
   <Line type="monotone" dataKey="amountReceived" name='ontvangen' stroke="#8884d8" activeDot={{r: 8}}/>
   <Line type="monotone" dataKey="amountPaid" name='betaald' stroke="#82ca9d" />
 </LineChart>
 </ResponsiveContainer>

export default QuarterStatisticsChart
