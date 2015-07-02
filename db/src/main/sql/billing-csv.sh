#!/bin/bash
#
# usage: ./billing-csv.sh 1-2015
#
# output goes into directory /degapp/out
if [ -x /degapp/out ]
then
   OUTPUT=/degapp/out
   DB=degapp
elif [ -x /public/degage/out ]
then
   OUTPUT=/public/degage/out
   DB=degage
else
   OUTPUT=/root/degage-out
   DB=degage
fi
# mysql-related temp dir
TEMPDIR=/var/lib/mysql-files
trap "rm -f $TEMPDIR/*-[AE].tmp" EXIT
#
mysql $DB <<EOF

SELECT billing_id FROM billing WHERE billing_prefix='$1' INTO @id;
SELECT car_id, name, fuel, deprec, costs, total, sc
    FROM b_car_overview
    WHERE billing_id = @id
    ORDER BY car_id
INTO OUTFILE '$TEMPDIR/$$-E.tmp' FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"';
SELECT user_id, name, km, fuel, total, sc
    FROM b_user_overview
    WHERE billing_id = @id
    ORDER BY user_id
INTO OUTFILE '$TEMPDIR/$$-A.tmp' FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"';
EOF
#
cat >$OUTPUT/E$1.csv <<EOF
Auto ID,Auto NAAM,Saldo brandstof,Recup afschrijving,Recup kosten,Totaal (in eurocent),
EOF
cat < $TEMPDIR/$$-E.tmp >> $OUTPUT/E$1.csv
#
cat >$OUTPUT/A$1.csv <<EOF
Gebruiker ID,Gebruiker NAAM,Kost Kilometers,Brandstof betaal,Totaal (in eurocent),
EOF
cat < $TEMPDIR/$$-A.tmp >> $OUTPUT/A$1.csv
#


