  -- Initialization of the templates

  -- INSERT INTO templatetags(template_tag_body) VALUE (?)

  INSERT INTO templatetags(template_tag_body) VALUE ("user_firstname");
  INSERT INTO templatetags(template_tag_body) VALUE ("user_lastname");

  INSERT INTO templatetags(template_tag_body) VALUE ("verification_url");
  INSERT INTO templatetags(template_tag_body) VALUE ("password_reset_url");

  INSERT INTO templatetags(template_tag_body) VALUE ("infosession_date");
  INSERT INTO templatetags(template_tag_body) VALUE ("infosession_address");

  INSERT INTO templatetags(template_tag_body) VALUE ("reservation_from");
  INSERT INTO templatetags(template_tag_body) VALUE ("reservation_to");
  INSERT INTO templatetags(template_tag_body) VALUE ("reservation_user_firstname");
  INSERT INTO templatetags(template_tag_body) VALUE ("reservation_user_lastname");
  INSERT INTO templatetags(template_tag_body) VALUE ("reservation_url");
  INSERT INTO templatetags(template_tag_body) VALUE ("reservation_car_address");
  INSERT INTO templatetags(template_tag_body) VALUE ("reservation_reason");

  INSERT INTO templatetags(template_tag_body) VALUE ("comment");

  INSERT INTO templatetags(template_tag_body) VALUE ("car_name");
  INSERT INTO templatetags(template_tag_body) VALUE ("amount");
  INSERT INTO templatetags(template_tag_body) VALUE ("car_cost_description");
  INSERT INTO templatetags(template_tag_body) VALUE ("car_cost_time");
  
  INSERT INTO templatetags(template_tag_body) VALUE ("admin_name");

  #-- templates
  #INSERT INTO templates(template_id, template_title, template_body) VALUES (?,?)

  #--Verificatie
  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  1,
  "Verificatie",
  "Beste toekomstige gebruiker,<br>
  <br>
  Klik op onderstaande link om verder te gaan met de registratie:<br>
  %verification_url% <br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Verificatie" AND template_tag_body = "verification_url";

  #--Welkom
  INSERT INTO templates(template_id, template_title, template_body, template_send_mail) VALUES (
  2,
  "Welkom",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  Welkom bij Dégage!<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Welkom" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Welkom" AND template_tag_body = "user_lastname";

  #--Infosessie ingeschreven
  INSERT INTO templates(template_id, template_title, template_body) VALUES (
  3,
  "Infosessie ingeschreven",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  Je hebt je ingeschreven voor een infosessie op %infosession_date%. <br>
  Deze infosessie zal doorgaan op het volgende adres:<br>
  %infosession_address%<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "infosession_address";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "infosession_date";

  #--Reservatie bevestigen
  INSERT INTO templates(template_id, template_title, template_body) VALUES (
  4,
  "Reservatie bevestigen",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  %reservation_user_firstname% %reservation_user_lastname% wil jouw auto reserven van %reservation_from% tot %reservation_to%.<br>
  <br>
  Gelieve deze reservatie zo snel mogelijk goed te keuren. Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>
  <br>
  Commentaar van de lener: %comment%.
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_from";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_to";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_url";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "comment";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_user_lastname";

  #--Reservatie bevestigd
  INSERT INTO templates(template_id, template_title, template_body) VALUES (
  5,
  "Reservatie bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Jouw reservatie is bevestigd, de auto is gereserveerd van %reservation_from% tot %reservation_to%.<br>
  <br>
  Adres van de auto:<br>
  %reservation_car_address%<br>
  <br>
  Opmerkingen door de eigenaar:<br>
  <br>
  <i>%reservation_remarks%</i><br>
  <br>
  Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_from";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_to";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_car_address";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_remarks";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_url";

  #--Reservatie geweigerd
  INSERT INTO templates(template_id, template_title, template_body) VALUES (
  6,
  "Reservatie geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Jouw reservatie, van %reservation_from% tot %reservation_to%, werd geweigerd door de eigenaar om volgende reden:<br>
  <br>
  <i>%reservation_reason%</i><br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "reservation_from";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "reservation_to";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "reservation_reason";

  #--Wachtwoord reset

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  7,
  "Wachtwoord reset",
  "Beste %user_firstname% %user_lastname%,<br>

  Klik op onderstaande link om een nieuw wachtwoord te kiezen.<br>
  %password_reset_url%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "password_reset_url";

 INSERT INTO `templates` (`template_id`, `template_title`, `template_subject`, `template_body`, `template_send_mail`, `template_send_mail_changeable`, `template_created_at`, `template_updated_at`) VALUES (8, 'Algemene voorwaarden', 'Bericht van Dégage!', '<div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Autodelensysteem&nbsp;</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">In ons systeem zijn er mensen mét auto en mensen zonder auto. Toch hebben ze allemaal een auto ter beschikking als ze dat willen (en op tijd reserveren). Eén auto wordt dus door verschillende mensen gebruikt.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Contract&nbsp;</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Als gebruiker bij Dégage! onderteken je een contract, waarin alles wat op deze website uitgelegd wordt, nog eens duidelijk vermeld staat. Je verklaart je dus akkoord met de regeling.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Eigenaars</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">schieten alle kosten voor hun auto voor en zijn verantwoordelijk voor verzekering, taksen, onderhoud</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">zorgen ervoor dat die verzekering, taksen in orde zijn en dat de auto in goede staat verkeert</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">krijgen een vergoeding per kilometer dat de wagen door iemand anders gebruikt wordt en kunnen zo de gemaakte kosten terugwinnen.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">zijn omnium verzekerd door het interne Dégage! waarborgensysteem</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">regelen zelf de reservaties van hun wagen</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Gebruikers</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">betalen een kilometervergoeding die overeenkomt met de reële kosten per kilometer;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">betalen een waarborg maar geen abonnementsgeld;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">behandelen de auto als een \'goede huisvader\' (m/v);</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">houden zich aan het contract dat ze ondertekenden;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">blijven minimaal 1 jaar lid;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">reserveren een auto als ze die nodig hebben en spreken af met de eigenaar voor sleutel;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">vullen na gebruik het bonnetje in;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Goede huisvader (m/v)</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"> Dit betekent dat men als een normaal voorzichtig mens de goederen behoudt en beheert en de wet respecteert. De hoofdverplichting van de gebruiker is dat de auto in dezelfde staat moet worden teruggegeven.&nbsp;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Euvels en gebreken meldt de gebruiker aan de eigenaar.&nbsp;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">De gebruiker respecteert de verkeersregels. Als hij die overtreedt, moet hij en hij alleen de boete betalen.&nbsp;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Een goede huisvader m/v rijdt niet onder invloed en niet te snel. Als de gebruiker die regel overtreedt en een ongeval heeft, moet hij alle kosten betalen. De verzekering is in dat geval genadeloos: ze kan alle schade aan derden op de gebruiker verhalen. Onnodig te zeggen dat die kosten kunnen oplopen tot een bedrag dat een mens van zijn leven niet kan verdienen.&nbsp;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Elke eigenaar heeft een gebruiksreglement voor zijn auto. Daarin staan regels die de eigenaar belangrijk vindt: of je mag roken bijvoorbeeld en eigenaardigheden van de auto zoals de choke, de radio, welke sleutel voor welk sleutelgat. Gebruikers respecteren dit reglement.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>\'Intern verzekeringssysteem\'</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">bij een schadegeval kleine gebruikers dragen minder bij dan grote gebruikers aangezien hun waarborg lager is</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">hoe meer leden er zijn, hoe groter de som van de waarborgen wordt. Er is veel kans dat slechts een klein deel van je waarborg moet gebruikt worden voor een schadegeval. Anderszijds is het ook zo dat je zelf ook van dit waarborgsysteem kan genieten als je een ongeluk tegenkomt : je moet zelf niet de volledige waarde van de auto aan de eigenaar terugbetalen.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Als er nog geld in reserve is op de Dégage!-rekening, kan dit eventueel ook gebruikt worden voor het weer aanzuiveren van de waarborgen</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Je loopt dus inderdaad het risico (een deel van) je waarborg kwijt te raken. Maar de waarborgsommen zijn zeer democratisch (vinden we zelf) voor het quasi onbeperkt gebruik van meerdere auto\'s.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Particulier&nbsp;</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Er bestaan verschillende systemen om aan autodelen te doen, ook commerciële. Dat is Dégage! in ieder geval niet, er wordt geen winst gemaakt. Binnen de niet-commerciële initiatieven zijn er nog verschillen. Bij de links kan je doorklikken naar sites van andere systemen. Dégage! komt hierop neer: de eigenaars blijven ten allen tijde eigenaar. In sommige andere systemen worden de auto\'s eigendom van het collectief. Niet zo bij ons. Dégage! is een uitgekiend systeem waarbij zowel eigenaars als gebruikers voordeel doen en waarbij een minimum aan administratie en dergelijke komt kijken.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Systeem&nbsp;</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Geleed en geordend geheel, complex, geschikt volgens een ordenend beginsel, syn. stelsel.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Voorschot&nbsp;</b></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Het voorschot werd in 2011 afgeschaft. Betaalde voorschotten worden bij de waarborg gevoegd.</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><br></span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;"><b>Waarborg</b>&nbsp;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Nieuwe gebruikers betalen bij Dégage! 75 € waarborg als ze lid worden. Na drie maanden wordt die waarborg aangepast aan de hoogte van je verbruik Na die eerste aanpassing wordt de waarborgsom elk jaar aangepast als dat nodig is.&nbsp;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">75 € is het laagste bedrag; voor wie minder dan 500 kilometer per drie maanden rijdt. Wie meer rijdt, betaalt meer.&nbsp;</span></font></div><div><font color="#696969" face="Verdana, sans-serif"><span style="font-size: 12px; line-height: normal;">Gebruikers die het systeem verlaten, krijgen hun waarborg geheel of gedeeltelijk terug. De waarborgsommen worden gebruikt bij ongevallen of andere onvoorziene omstandigheden, als een soort interne verzekering. Als die sommen onaangeroerd blijven, kan Dégage! de volledige waarborg terugbetalen.</span></font></div>', b'0', b'0', '2014-05-14 14:18:57', '2014-05-14 16:03:41');

  #--Lidmaatschap bevestigd

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  9,
  "Lidmaatschap bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Gefeliciteerd! Jouw lidmaatschap bij Dégage werd zonet bevestgd.<br>
  %comment%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "comment";

  #--Lidmaatschap geweigerd

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  10,
  "Lidmaatschap geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Jouw lidmaatschap bij Dégage werd zonet geweigerd.<br>
  %comment%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap geweigerd" AND template_tag_body = "comment";

  #--Autokost bevestigd

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  11,
  "Autokost bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Jouw autokost werd zonet bevestigd door een admin.<br>
  %car_name%
  <br>
  %car_cost_description%
  <br>
  %amount%
  <br>
  %car_cost_time%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "car_name";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "car_cost_description";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "amount";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "car_cost_time";

  #--Autokost geweigerd

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  12,
  "Autokost geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Jouw autokost werd helaas geweigerd door een admin.<br>
  %car_name%
  <br>
  %car_cost_description%
  <br>
  %amount%
  <br>
  %car_cost_time%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "car_name";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "car_cost_description";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "amount";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "car_cost_time";


  #--Tankbeurt bevestigd

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  13,
  "Tankbeurt bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Jouw tankbeurt werd zonet bevestigd door de auto-eigenaar.<br>
  %car_name%
  <br>
  %amount%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "car_name";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "amount";


   #--Tankbeurt geweigerd

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  14,
  "Tankbeurt geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Jouw tankbeurt werd helaas geweigerd door de auto-eigenaar.<br>
  %car_name%
  <br>
  %amount%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "car_name";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "amount";

  #-- Reminder mail
  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  15,
  "Ongelezen berichten",
  "Beste %user_firstname% %user_lastname%,<br>

  Je hebt ongelezen berichten. Gelieve in te loggen op jouw Dégage-account.<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ongelezen berichten" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ongelezen berichten" AND template_tag_body = "user_lastname";

  #--Tankbeurt aanvraag

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  16,
  "Tankbeurt aanvraag",
  "Beste %user_firstname% %user_lastname%,<br>

  Er werd zonet een tankbeurt ingegeven voor jouw wagen. Gelieve deze zo snel mogelijk goed te keuren.<br>
  %car_name%
  <br>
  %amount%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "car_name";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "amount";

  #--Autokost aanvraag

  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable, template_send_mail) VALUES (
  17,
  "Autokost aanvraag",
  "Beste %user_firstname% %user_lastname%,<br>

  Er werd zonet een autokost ingegeven voor de volgende wagen. Gelieve deze zo snel mogelijk goed te keuren.<br>
  %car_name%
  <br>
  %car_cost_description%
  <br>
  %amount%
  <br>
  %car_cost_time%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0,0);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "car_name";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "car_cost_description";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "amount";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "car_cost_time";
  
  
  INSERT INTO templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  18,
  "Contractmanager toegewezen",
  "Beste %user_firstname% %user_lastname%,<br>

  %admin_name% werd zojuist toegewezen als jouw contractverantwoordelijke. Deze persoon zal jouw registratie verder afhandelen.<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Contractmanager toegewezen" AND template_tag_body = "admin_name";

  INSERT INTO templates(template_id, template_title, template_body) VALUES (
  19,
  "Ritdetails aangevuld",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  Er zijn zonet nieuwe ritdetails aangevuld voor uw auto voor de reservatie van %reservation_from% tot %reservation_to% door %reservation_user_firstname% %reservation_user_lastname%<br>
  <br>
  Gelieve deze details zo snel mogelijk goed te keuren. Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ritdetails aangevuld" AND template_tag_body = "user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ritdetails aangevuld" AND template_tag_body = "user_lastname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ritdetails aangevuld" AND template_tag_body = "reservation_from";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ritdetails aangevuld" AND template_tag_body = "reservation_to";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ritdetails aangevuld" AND template_tag_body = "reservation_url";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ritdetails aangevuld" AND template_tag_body = "reservation_user_firstname";
  INSERT INTO templatetagassociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM templates, templatetags WHERE template_title = "Ritdetails aangevuld" AND template_tag_body = "reservation_user_lastname";
