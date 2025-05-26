package airport;

import airport.core.controllers.FlightController;
import airport.core.controllers.LocationController;
import javax.swing.JOptionPane;
import airport.core.controllers.PassengerController;
import airport.core.controllers.PlaneController;
import airport.core.models.Location;
import airport.core.models.Passenger;
import airport.core.models.Plane;
import java.awt.Color;
import java.time.LocalDateTime;
import javax.swing.table.DefaultTableModel;
import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AirportFrame extends javax.swing.JFrame {

    private int x, y;
    private PassengerController passengerController;
    private FlightController flightController;
    private LocationController locationController;
    private PlaneController planeController;
    private Map<String, Long> nombreIdMap = new HashMap<>();

    public AirportFrame() {
        initComponents();

        locationController = new LocationController();
        planeController = new PlaneController();
        flightController = new FlightController(
                planeController.getAllPlanes().getObject(),
                locationController.getAllLocations().getObject()
        );
        passengerController = new PassengerController(
                flightController.getAllFlights().getObject()
        );

        passengerController.addObserver(() -> RefreshButton_ShowAllPassengers.doClick());
        planeController.addObserver(() -> RefreshButton_ShowAllPlanes.doClick());
        locationController.addObserver(() -> RefreshButton_ShowAllLocations.doClick());
        flightController.addObserver(() -> RefreshButton_ShowAllFlights.doClick());

        this.setBackground(new Color(0, 0, 0, 0));
        this.setLocationRelativeTo(null);

        this.generateMonths();
        this.generateDays();
        this.generateHours();
        this.generateMinutes();
        this.blockPanels();
        this.cargarUsuariosEnComboBox();
        this.cargarVuelosEnComboBox();
        this.cargarAvionesEnComboBox();
        this.cargarLocalizacionesEnComboBox();
    }

    private void blockPanels() {
        //9, 11
        for (int i = 1; i < jTabbedPane1.getTabCount(); i++) {
            if (i != 9 && i != 11) {
                jTabbedPane1.setEnabledAt(i, false);
            }
        }
    }

    private void generateMonths() {
        for (int i = 1; i < 13; i++) {
            SelectBirthdayMonth_PassengerRegistration.addItem("" + i);
            DepartureDate_SelectMonth_FlightRegistration.addItem("" + i);
            SelectBirthdayMonth_UpdateInfo.addItem("" + i);
        }
    }

    private void generateDays() {
        for (int i = 1; i < 32; i++) {
            SelectBirthdayDay_PassengerRegistration.addItem("" + i);
            DepartureDate_SelectDay_FlightRegistration.addItem("" + i);
            SelectBirthdayDay_UpdateInfo.addItem("" + i);
        }
    }

    private void generateHours() {
        for (int i = 0; i < 24; i++) {
            DepartureDate_SelectHour_FlightRegistration.addItem("" + i);
            ArrivalDuration_SelectHour_FlightRegistration.addItem("" + i);
            ScaleDuration_SelectHour_FlightRegistration.addItem("" + i);
            SelectHour_DelayFlight.addItem("" + i);
        }
    }

    private void generateMinutes() {
        for (int i = 0; i < 60; i++) {
            DepartureDate_SelectMinute_FlightRegistration.addItem("" + i);
            ArrivalDuration_SelectMinute_FlightRegistration.addItem("" + i);
            ScaleDuration_SelectMinute_FlightRegistration.addItem("" + i);
            SelectMinute_DelayFlight.addItem("" + i);
        }
    }

    private void cargarUsuariosEnComboBox() {
        userSelect.removeAllItems();
        nombreIdMap.clear(); // Limpiar antes de recargar

        Response<List<Passenger>> response = passengerController.getAllPassengers();

        if (response.getStatus() == Status.OK) {
            for (Passenger p : response.getObject()) {
                String nombre = p.getFullname();
                userSelect.addItem(nombre);
                nombreIdMap.put(nombre, p.getId());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error cargando usuarios: " + response.getMessage());
        }
    }

    private void cargarAvionesEnComboBox() {
        SelectPlane_FlightRegistration.removeAllItems();

        Response<List<Plane>> response = planeController.getAllPlanes();

        if (response.getStatus() == Status.OK) {
            for (Plane plane : response.getObject()) {
                SelectPlane_FlightRegistration.addItem(plane.getId());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error cargando aviones: " + response.getMessage());
        }
    }

    private void cargarLocalizacionesEnComboBox() {
        SelectDepartureLocation_FlightRegistration.removeAllItems(); // Salida
        SelectArrivalLocation_FlightRegistration.removeAllItems(); // Llegada
        ScaleLocation_FlightRegistration.removeAllItems(); // Escala

        Response<List<Location>> response = locationController.getAllLocations();

        if (response.getStatus() == Status.OK) {
            ScaleLocation_FlightRegistration.addItem("Ninguna");
            for (Location loc : response.getObject()) {
                SelectDepartureLocation_FlightRegistration.addItem(loc.getAirportId());
                SelectArrivalLocation_FlightRegistration.addItem(loc.getAirportId());
                ScaleLocation_FlightRegistration.addItem(loc.getAirportId());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error cargando localizaciones: " + response.getMessage());
        }
    }

    private void cargarVuelosEnComboBox() {
        SelectFlight_AddToFlight.removeAllItems(); // Limpia el ComboBox de vuelos
        SelectID_DelayFlight.removeAllItems();

        Response<List<Flight>> response = flightController.getAllFlights();
        if (response.getStatus() == Status.OK) {
            for (Flight flight : response.getObject()) {
                SelectFlight_AddToFlight.addItem(flight.getId()); // Muestra solo el ID del vuelo
                SelectID_DelayFlight.addItem(flight.getId());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error cargando vuelos: " + response.getMessage());
        }
    }

    private void refrescarVuelosDePasajero(long passengerId) {
        Response<List<Flight>> response = flightController.getFlightsByPassengerId(passengerId);

        if (response.getStatus() != Status.OK) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) Table_ShowMyFlights.getModel();
        model.setRowCount(0);

        for (Flight flight : response.getObject()) {
            model.addRow(new Object[]{
                flight.getId(),
                flight.getDepartureDate(),
                flight.calculateArrivalDate()
            });
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRound1 = new airport.PanelRound();
        panelRound2 = new airport.PanelRound();
        jButton13 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        UserOption_Administration = new javax.swing.JRadioButton();
        AdministratorOption_Administration = new javax.swing.JRadioButton();
        userSelect = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        CountryIdentifier_PassengerRegistration = new javax.swing.JTextField();
        Id_PassengerRegistration = new javax.swing.JTextField();
        BirhtdayYear_PassengerRegistration = new javax.swing.JTextField();
        Country_PassengerRegistration = new javax.swing.JTextField();
        Phone_PassengerRegistration = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        LastName_PassengerRegistration = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        SelectBirthdayMonth_PassengerRegistration = new javax.swing.JComboBox<>();
        FirstName_PassengerResgistration = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        SelectBirthdayDay_PassengerRegistration = new javax.swing.JComboBox<>();
        RegisterButton_PassengerRegistration = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        Id_AirplaneRegistration = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        Brand_AirplaneRegistration = new javax.swing.JTextField();
        Model_AirplaneRegistration = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        MaxCapacity_AirplaneRegistration = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        Airline_AirplaneRegistration = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        CreateButton_AirplaneRegistration = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        AirportID_LocationRegistration = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        AirportName_LocationRegistration = new javax.swing.JTextField();
        AirportCity_LocationRegistration = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        AirportCountry_LocationRegistration = new javax.swing.JTextField();
        AirportLatitude_LocationRegistration = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        AirportLongitude_LocationRegistration = new javax.swing.JTextField();
        CreateButton_LocationRegistration = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        ID_FlightRegistration = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        SelectPlane_FlightRegistration = new javax.swing.JComboBox<>();
        SelectDepartureLocation_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel24 = new javax.swing.JLabel();
        SelectArrivalLocation_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        ScaleLocation_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        DepartureDate_Day_FlightRegistration = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        DepartureDate_SelectMonth_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        DepartureDate_SelectDay_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel32 = new javax.swing.JLabel();
        DepartureDate_SelectHour_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        DepartureDate_SelectMinute_FlightRegistration = new javax.swing.JComboBox<>();
        ArrivalDuration_SelectHour_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel34 = new javax.swing.JLabel();
        ArrivalDuration_SelectMinute_FlightRegistration = new javax.swing.JComboBox<>();
        jLabel35 = new javax.swing.JLabel();
        ScaleDuration_SelectHour_FlightRegistration = new javax.swing.JComboBox<>();
        ScaleDuration_SelectMinute_FlightRegistration = new javax.swing.JComboBox<>();
        CreateButton_FlightRegistration = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        ID_UpdateInfo = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        FirstName_UpdateInfo = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        LastName_UpdateInfo = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        BirthdayYear_UpdateInfo = new javax.swing.JTextField();
        SelectBirthdayMonth_UpdateInfo = new javax.swing.JComboBox<>();
        SelectBirthdayDay_UpdateInfo = new javax.swing.JComboBox<>();
        Phone_UpdateInfo = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        CountryIdentifier_UpdateInfo = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        Country_UpdateInfo = new javax.swing.JTextField();
        UpdateButton_UpdateInfo = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        ID_AddToFlight = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        SelectFlight_AddToFlight = new javax.swing.JComboBox<>();
        Add_AddToFlight = new javax.swing.JButton();
        RefreshButton_ShowMyFlights = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Table_ShowMyFlights = new javax.swing.JTable();
        RefreshButton_ShowAllMyFlights = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table_ShowAllPassengers = new javax.swing.JTable();
        RefreshButton_ShowAllPassengers = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        Table_ShowAllFlights = new javax.swing.JTable();
        RefreshButton_ShowAllFlights = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        RefreshButton_ShowAllPlanes = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        Table_ShowAllPlanes = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        Table_ShowAllLocations = new javax.swing.JTable();
        RefreshButton_ShowAllLocations = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        SelectHour_DelayFlight = new javax.swing.JComboBox<>();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        SelectID_DelayFlight = new javax.swing.JComboBox<>();
        jLabel48 = new javax.swing.JLabel();
        SelectMinute_DelayFlight = new javax.swing.JComboBox<>();
        DelayButton_DelayFlight = new javax.swing.JButton();
        panelRound3 = new airport.PanelRound();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        panelRound1.setRadius(40);
        panelRound1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelRound2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panelRound2MouseDragged(evt);
            }
        });
        panelRound2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelRound2MousePressed(evt);
            }
        });

        jButton13.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jButton13.setText("X");
        jButton13.setBorderPainted(false);
        jButton13.setContentAreaFilled(false);
        jButton13.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelRound2Layout = new javax.swing.GroupLayout(panelRound2);
        panelRound2.setLayout(panelRound2Layout);
        panelRound2Layout.setHorizontalGroup(
            panelRound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRound2Layout.createSequentialGroup()
                .addContainerGap(1083, Short.MAX_VALUE)
                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );
        panelRound2Layout.setVerticalGroup(
            panelRound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound2Layout.createSequentialGroup()
                .addComponent(jButton13)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        panelRound1.add(panelRound2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, -1));

        jTabbedPane1.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        UserOption_Administration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        UserOption_Administration.setText("User");
        UserOption_Administration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserOption_AdministrationActionPerformed(evt);
            }
        });
        jPanel1.add(UserOption_Administration, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 230, -1, -1));

        AdministratorOption_Administration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        AdministratorOption_Administration.setText("Administrator");
        AdministratorOption_Administration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AdministratorOption_AdministrationActionPerformed(evt);
            }
        });
        jPanel1.add(AdministratorOption_Administration, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 164, -1, -1));

        userSelect.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        userSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select User" }));
        userSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userSelectActionPerformed(evt);
            }
        });
        jPanel1.add(userSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 300, 130, -1));

        jTabbedPane1.addTab("Administration", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel1.setText("Country:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 400, -1, -1));

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel2.setText("ID:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, -1, -1));

        jLabel3.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel3.setText("First Name:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 160, -1, -1));

        jLabel4.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel4.setText("Last Name:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 220, -1, -1));

        jLabel5.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel5.setText("Birthdate:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 280, -1, -1));

        jLabel6.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel6.setText("+");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 340, 20, -1));

        CountryIdentifier_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(CountryIdentifier_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 340, 50, -1));

        Id_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(Id_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, 130, -1));

        BirhtdayYear_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(BirhtdayYear_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 280, 90, -1));

        Country_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(Country_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 400, 130, -1));

        Phone_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(Phone_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 340, 130, -1));

        jLabel7.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel7.setText("Phone:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 340, -1, -1));

        jLabel8.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel8.setText("-");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 280, 30, -1));

        LastName_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(LastName_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 220, 130, -1));

        jLabel9.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel9.setText("-");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 340, 30, -1));

        SelectBirthdayMonth_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectBirthdayMonth_PassengerRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Month" }));
        jPanel2.add(SelectBirthdayMonth_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 280, -1, -1));

        FirstName_PassengerResgistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(FirstName_PassengerResgistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 160, 130, -1));

        jLabel10.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel10.setText("-");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 280, 30, -1));

        SelectBirthdayDay_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectBirthdayDay_PassengerRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Day" }));
        jPanel2.add(SelectBirthdayDay_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 280, -1, -1));

        RegisterButton_PassengerRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        RegisterButton_PassengerRegistration.setText("Register");
        RegisterButton_PassengerRegistration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RegisterButton_PassengerRegistrationActionPerformed(evt);
            }
        });
        jPanel2.add(RegisterButton_PassengerRegistration, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 480, -1, -1));

        jTabbedPane1.addTab("Passenger registration", jPanel2);

        jPanel3.setLayout(null);

        jLabel11.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel11.setText("ID:");
        jPanel3.add(jLabel11);
        jLabel11.setBounds(53, 96, 22, 25);

        Id_AirplaneRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(Id_AirplaneRegistration);
        Id_AirplaneRegistration.setBounds(180, 93, 130, 31);

        jLabel12.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel12.setText("Brand:");
        jPanel3.add(jLabel12);
        jLabel12.setBounds(53, 157, 50, 25);

        Brand_AirplaneRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(Brand_AirplaneRegistration);
        Brand_AirplaneRegistration.setBounds(180, 154, 130, 31);

        Model_AirplaneRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(Model_AirplaneRegistration);
        Model_AirplaneRegistration.setBounds(180, 213, 130, 31);

        jLabel13.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel13.setText("Model:");
        jPanel3.add(jLabel13);
        jLabel13.setBounds(53, 216, 55, 25);

        MaxCapacity_AirplaneRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(MaxCapacity_AirplaneRegistration);
        MaxCapacity_AirplaneRegistration.setBounds(180, 273, 130, 31);

        jLabel14.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel14.setText("Max Capacity:");
        jPanel3.add(jLabel14);
        jLabel14.setBounds(53, 276, 109, 25);

        Airline_AirplaneRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(Airline_AirplaneRegistration);
        Airline_AirplaneRegistration.setBounds(180, 333, 130, 31);

        jLabel15.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel15.setText("Airline:");
        jPanel3.add(jLabel15);
        jLabel15.setBounds(53, 336, 70, 25);

        CreateButton_AirplaneRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        CreateButton_AirplaneRegistration.setText("Create");
        CreateButton_AirplaneRegistration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateButton_AirplaneRegistrationActionPerformed(evt);
            }
        });
        jPanel3.add(CreateButton_AirplaneRegistration);
        CreateButton_AirplaneRegistration.setBounds(490, 480, 120, 40);

        jTabbedPane1.addTab("Airplane registration", jPanel3);

        jLabel16.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel16.setText("Airport ID:");

        AirportID_LocationRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel17.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel17.setText("Airport name:");

        AirportName_LocationRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        AirportName_LocationRegistration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AirportName_LocationRegistrationActionPerformed(evt);
            }
        });

        AirportCity_LocationRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel18.setText("Airport city:");

        jLabel19.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel19.setText("Airport country:");

        AirportCountry_LocationRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        AirportLatitude_LocationRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel20.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel20.setText("Airport latitude:");

        jLabel21.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel21.setText("Airport longitude:");

        AirportLongitude_LocationRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        CreateButton_LocationRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        CreateButton_LocationRegistration.setText("Create");
        CreateButton_LocationRegistration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateButton_LocationRegistrationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21))
                        .addGap(80, 80, 80)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(AirportLongitude_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AirportID_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AirportName_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AirportCity_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AirportCountry_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AirportLatitude_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(515, 515, 515)
                        .addComponent(CreateButton_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(515, 515, 515))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(36, 36, 36)
                        .addComponent(jLabel17)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel18)
                        .addGap(35, 35, 35)
                        .addComponent(jLabel19)
                        .addGap(35, 35, 35)
                        .addComponent(jLabel20))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(AirportID_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(AirportName_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(AirportCity_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(AirportCountry_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(AirportLatitude_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(44, 44, 44)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(AirportLongitude_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(CreateButton_LocationRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
        );

        jTabbedPane1.addTab("Location registration", jPanel13);

        jLabel22.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel22.setText("ID:");

        ID_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel23.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel23.setText("Plane:");

        SelectPlane_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectPlane_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Plane" }));

        SelectDepartureLocation_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectDepartureLocation_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Location" }));

        jLabel24.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel24.setText("Departure location:");

        SelectArrivalLocation_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectArrivalLocation_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Location" }));

        jLabel25.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel25.setText("Arrival location:");

        jLabel26.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel26.setText("Scale location:");

        ScaleLocation_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        ScaleLocation_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Location" }));

        jLabel27.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel27.setText("Duration:");

        jLabel28.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel28.setText("Duration:");

        jLabel29.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel29.setText("Departure date:");

        DepartureDate_Day_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel30.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel30.setText("-");

        DepartureDate_SelectMonth_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DepartureDate_SelectMonth_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Month" }));

        jLabel31.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel31.setText("-");

        DepartureDate_SelectDay_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DepartureDate_SelectDay_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Day" }));

        jLabel32.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel32.setText("-");

        DepartureDate_SelectHour_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DepartureDate_SelectHour_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        jLabel33.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel33.setText("-");

        DepartureDate_SelectMinute_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DepartureDate_SelectMinute_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        ArrivalDuration_SelectHour_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        ArrivalDuration_SelectHour_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        jLabel34.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel34.setText("-");

        ArrivalDuration_SelectMinute_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        ArrivalDuration_SelectMinute_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        jLabel35.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel35.setText("-");

        ScaleDuration_SelectHour_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        ScaleDuration_SelectHour_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        ScaleDuration_SelectMinute_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        ScaleDuration_SelectMinute_FlightRegistration.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        CreateButton_FlightRegistration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        CreateButton_FlightRegistration.setText("Create");
        CreateButton_FlightRegistration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateButton_FlightRegistrationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ScaleLocation_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(SelectArrivalLocation_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addGap(46, 46, 46)
                        .addComponent(SelectDepartureLocation_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(ID_FlightRegistration)
                            .addComponent(SelectPlane_FlightRegistration, 0, 130, Short.MAX_VALUE))))
                .addGap(45, 45, 45)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel28)
                    .addComponent(jLabel29))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(DepartureDate_Day_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(DepartureDate_SelectMonth_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(DepartureDate_SelectDay_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(DepartureDate_SelectHour_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(DepartureDate_SelectMinute_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(30, 30, 30))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(ArrivalDuration_SelectHour_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(ArrivalDuration_SelectMinute_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(ScaleDuration_SelectHour_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(ScaleDuration_SelectMinute_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(CreateButton_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(530, 530, 530))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel22))
                    .addComponent(ID_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(SelectPlane_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DepartureDate_SelectHour_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32)
                    .addComponent(jLabel33)
                    .addComponent(DepartureDate_SelectMinute_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel24)
                                .addComponent(SelectDepartureLocation_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel29))
                            .addComponent(DepartureDate_Day_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(DepartureDate_SelectMonth_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel30)
                            .addComponent(jLabel31)
                            .addComponent(DepartureDate_SelectDay_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel25)
                                .addComponent(SelectArrivalLocation_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel28))
                            .addComponent(ArrivalDuration_SelectHour_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel34)
                            .addComponent(ArrivalDuration_SelectMinute_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ScaleDuration_SelectHour_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35)
                            .addComponent(ScaleDuration_SelectMinute_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel26)
                                .addComponent(ScaleLocation_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel27)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                .addComponent(CreateButton_FlightRegistration, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        jTabbedPane1.addTab("Flight registration", jPanel4);

        jLabel36.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel36.setText("ID:");

        ID_UpdateInfo.setEditable(false);
        ID_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        ID_UpdateInfo.setEnabled(false);

        jLabel37.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel37.setText("First Name:");

        FirstName_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel38.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel38.setText("Last Name:");

        LastName_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel39.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel39.setText("Birthdate:");

        BirthdayYear_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        SelectBirthdayMonth_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectBirthdayMonth_UpdateInfo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Month" }));

        SelectBirthdayDay_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectBirthdayDay_UpdateInfo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Day" }));

        Phone_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel40.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel40.setText("-");

        CountryIdentifier_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel41.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel41.setText("+");

        jLabel42.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel42.setText("Phone:");

        jLabel43.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel43.setText("Country:");

        Country_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        UpdateButton_UpdateInfo.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        UpdateButton_UpdateInfo.setText("Update");
        UpdateButton_UpdateInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateButton_UpdateInfoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel36)
                                .addGap(108, 108, 108)
                                .addComponent(ID_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel37)
                                .addGap(41, 41, 41)
                                .addComponent(FirstName_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel38)
                                .addGap(43, 43, 43)
                                .addComponent(LastName_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addGap(55, 55, 55)
                                .addComponent(BirthdayYear_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(SelectBirthdayMonth_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(SelectBirthdayDay_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel42)
                                .addGap(56, 56, 56)
                                .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(CountryIdentifier_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(Phone_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel43)
                                .addGap(63, 63, 63)
                                .addComponent(Country_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(507, 507, 507)
                        .addComponent(UpdateButton_UpdateInfo)))
                .addContainerGap(598, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(ID_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel37)
                    .addComponent(FirstName_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel38)
                    .addComponent(LastName_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(BirthdayYear_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SelectBirthdayMonth_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SelectBirthdayDay_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel42)
                    .addComponent(jLabel41)
                    .addComponent(CountryIdentifier_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40)
                    .addComponent(Phone_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel43)
                    .addComponent(Country_UpdateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(UpdateButton_UpdateInfo)
                .addGap(113, 113, 113))
        );

        jTabbedPane1.addTab("Update info", jPanel5);

        ID_AddToFlight.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        ID_AddToFlight.setEnabled(false);
        ID_AddToFlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ID_AddToFlightActionPerformed(evt);
            }
        });

        jLabel44.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel44.setText("ID:");

        jLabel45.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel45.setText("Flight:");

        SelectFlight_AddToFlight.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectFlight_AddToFlight.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Flight" }));
        SelectFlight_AddToFlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectFlight_AddToFlightActionPerformed(evt);
            }
        });

        Add_AddToFlight.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        Add_AddToFlight.setText("Add");
        Add_AddToFlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Add_AddToFlightActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel44)
                    .addComponent(jLabel45))
                .addGap(79, 79, 79)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SelectFlight_AddToFlight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ID_AddToFlight, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(873, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Add_AddToFlight, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(509, 509, 509))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel44))
                    .addComponent(ID_AddToFlight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(SelectFlight_AddToFlight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 288, Short.MAX_VALUE)
                .addComponent(Add_AddToFlight, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85))
        );

        jTabbedPane1.addTab("Add to flight", jPanel6);

        Table_ShowMyFlights.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        Table_ShowMyFlights.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Departure Date", "Arrival Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(Table_ShowMyFlights);

        RefreshButton_ShowAllMyFlights.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        RefreshButton_ShowAllMyFlights.setText("Refresh");
        RefreshButton_ShowAllMyFlights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RefreshButton_ShowAllMyFlightsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RefreshButton_ShowMyFlightsLayout = new javax.swing.GroupLayout(RefreshButton_ShowMyFlights);
        RefreshButton_ShowMyFlights.setLayout(RefreshButton_ShowMyFlightsLayout);
        RefreshButton_ShowMyFlightsLayout.setHorizontalGroup(
            RefreshButton_ShowMyFlightsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RefreshButton_ShowMyFlightsLayout.createSequentialGroup()
                .addGap(269, 269, 269)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(337, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RefreshButton_ShowMyFlightsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(RefreshButton_ShowAllMyFlights)
                .addGap(527, 527, 527))
        );
        RefreshButton_ShowMyFlightsLayout.setVerticalGroup(
            RefreshButton_ShowMyFlightsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RefreshButton_ShowMyFlightsLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(RefreshButton_ShowAllMyFlights)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Show my flights", RefreshButton_ShowMyFlights);

        Table_ShowAllPassengers.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        Table_ShowAllPassengers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Birthdate", "Age", "Phone", "Country", "Num Flight"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(Table_ShowAllPassengers);

        RefreshButton_ShowAllPassengers.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        RefreshButton_ShowAllPassengers.setText("Refresh");
        RefreshButton_ShowAllPassengers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RefreshButton_ShowAllPassengersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(489, 489, 489)
                        .addComponent(RefreshButton_ShowAllPassengers))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1078, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(72, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(RefreshButton_ShowAllPassengers)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Show all passengers", jPanel8);

        Table_ShowAllFlights.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        Table_ShowAllFlights.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Departure Airport ID", "Arrival Airport ID", "Scale Airport ID", "Departure Date", "Arrival Date", "Plane ID", "Number Passengers"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(Table_ShowAllFlights);

        RefreshButton_ShowAllFlights.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        RefreshButton_ShowAllFlights.setText("Refresh");
        RefreshButton_ShowAllFlights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RefreshButton_ShowAllFlightsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(521, 521, 521)
                        .addComponent(RefreshButton_ShowAllFlights)))
                .addContainerGap(67, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(RefreshButton_ShowAllFlights)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Show all flights", jPanel9);

        RefreshButton_ShowAllPlanes.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        RefreshButton_ShowAllPlanes.setText("Refresh");
        RefreshButton_ShowAllPlanes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RefreshButton_ShowAllPlanesActionPerformed(evt);
            }
        });

        Table_ShowAllPlanes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Brand", "Model", "Max Capacity", "Airline", "Number Flights"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(Table_ShowAllPlanes);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(508, 508, 508)
                        .addComponent(RefreshButton_ShowAllPlanes))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(145, 145, 145)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 816, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(235, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(45, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(RefreshButton_ShowAllPlanes)
                .addGap(17, 17, 17))
        );

        jTabbedPane1.addTab("Show all planes", jPanel10);

        Table_ShowAllLocations.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Airport ID", "Airport Name", "City", "Country"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(Table_ShowAllLocations);

        RefreshButton_ShowAllLocations.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        RefreshButton_ShowAllLocations.setText("Refresh");
        RefreshButton_ShowAllLocations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RefreshButton_ShowAllLocationsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(508, 508, 508)
                        .addComponent(RefreshButton_ShowAllLocations))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(226, 226, 226)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 652, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(318, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(48, Short.MAX_VALUE)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(RefreshButton_ShowAllLocations)
                .addGap(17, 17, 17))
        );

        jTabbedPane1.addTab("Show all locations", jPanel11);

        SelectHour_DelayFlight.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectHour_DelayFlight.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        jLabel46.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel46.setText("Hours:");

        jLabel47.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel47.setText("ID:");

        SelectID_DelayFlight.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectID_DelayFlight.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ID" }));
        SelectID_DelayFlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectID_DelayFlightActionPerformed(evt);
            }
        });

        jLabel48.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel48.setText("Minutes:");

        SelectMinute_DelayFlight.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        SelectMinute_DelayFlight.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        DelayButton_DelayFlight.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DelayButton_DelayFlight.setText("Delay");
        DelayButton_DelayFlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelayButton_DelayFlightActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel48)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(SelectMinute_DelayFlight, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel47)
                            .addComponent(jLabel46))
                        .addGap(79, 79, 79)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SelectHour_DelayFlight, 0, 151, Short.MAX_VALUE)
                            .addComponent(SelectID_DelayFlight, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(820, 820, 820))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(DelayButton_DelayFlight)
                .addGap(531, 531, 531))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47)
                    .addComponent(SelectID_DelayFlight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(SelectHour_DelayFlight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48)
                    .addComponent(SelectMinute_DelayFlight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 307, Short.MAX_VALUE)
                .addComponent(DelayButton_DelayFlight)
                .addGap(33, 33, 33))
        );

        jTabbedPane1.addTab("Delay flight", jPanel12);

        panelRound1.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 41, 1150, 620));

        javax.swing.GroupLayout panelRound3Layout = new javax.swing.GroupLayout(panelRound3);
        panelRound3.setLayout(panelRound3Layout);
        panelRound3Layout.setHorizontalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1150, Short.MAX_VALUE)
        );
        panelRound3Layout.setVerticalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        panelRound1.add(panelRound3, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 660, 1150, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRound1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRound1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void panelRound2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelRound2MousePressed
        x = evt.getX();
        y = evt.getY();
    }//GEN-LAST:event_panelRound2MousePressed

    private void panelRound2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelRound2MouseDragged
        this.setLocation(this.getLocation().x + evt.getX() - x, this.getLocation().y + evt.getY() - y);
    }//GEN-LAST:event_panelRound2MouseDragged

    private void AdministratorOption_AdministrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AdministratorOption_AdministrationActionPerformed
        if (UserOption_Administration.isSelected()) {
            UserOption_Administration.setSelected(false);
            userSelect.setSelectedIndex(0);

        }
        for (int i = 1; i < jTabbedPane1.getTabCount(); i++) {
            jTabbedPane1.setEnabledAt(i, true);
        }

        jTabbedPane1.setEnabledAt(5, false);
        jTabbedPane1.setEnabledAt(6, false);
        jTabbedPane1.setEnabledAt(7, false);
    }//GEN-LAST:event_AdministratorOption_AdministrationActionPerformed

    private void UserOption_AdministrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UserOption_AdministrationActionPerformed
        if (AdministratorOption_Administration.isSelected()) {
            AdministratorOption_Administration.setSelected(false);
        }

        for (int i = 1; i < jTabbedPane1.getTabCount(); i++) {
            jTabbedPane1.setEnabledAt(i, false);
        }

        jTabbedPane1.setEnabledAt(9, true);
        jTabbedPane1.setEnabledAt(5, true);
        jTabbedPane1.setEnabledAt(6, true);
        jTabbedPane1.setEnabledAt(7, true);
        jTabbedPane1.setEnabledAt(11, true);

    }//GEN-LAST:event_UserOption_AdministrationActionPerformed

    private void RegisterButton_PassengerRegistrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegisterButton_PassengerRegistrationActionPerformed
        try {
            long id = Long.parseLong(Id_PassengerRegistration.getText());
            String firstname = FirstName_PassengerResgistration.getText();
            String lastname = LastName_PassengerRegistration.getText();
            String year = BirhtdayYear_PassengerRegistration.getText();
            String month = (String) SelectBirthdayMonth_PassengerRegistration.getSelectedItem();
            String day = (String) SelectBirthdayDay_PassengerRegistration.getSelectedItem();
            String phoneCode = CountryIdentifier_PassengerRegistration.getText();
            String phone = Phone_PassengerRegistration.getText();
            String country = Country_PassengerRegistration.getText();

            Response<Void> response = passengerController.updatePassenger(
                    id, firstname, lastname, year, month, day, phoneCode, phone, country);

            if (response.getStatus() == Status.OK) {
                JOptionPane.showMessageDialog(this, "Pasajero registrado correctamente");
                String nombreCompleto = firstname + " " + lastname;
                userSelect.addItem(nombreCompleto);
                nombreIdMap.put(nombreCompleto, id);
            } else if (response.getStatus() == Status.NO_CONTENT) {
                JOptionPane.showMessageDialog(this, "Ya existe un pasajero con estos datos");
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_RegisterButton_PassengerRegistrationActionPerformed

    private void CreateButton_AirplaneRegistrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateButton_AirplaneRegistrationActionPerformed
        String id = Id_AirplaneRegistration.getText().trim();
        String marca = Brand_AirplaneRegistration.getText().trim();
        String modelo = Model_AirplaneRegistration.getText().trim();
        String capacidadStr = MaxCapacity_AirplaneRegistration.getText().trim();
        String aerolinea = Airline_AirplaneRegistration.getText().trim();

        Response<Void> response = planeController.createPlane(id, marca, modelo, capacidadStr, aerolinea);

        if (response.getStatus() == Status.CREATED) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

            Id_AirplaneRegistration.setText("");
            Brand_AirplaneRegistration.setText("");
            Model_AirplaneRegistration.setText("");
            MaxCapacity_AirplaneRegistration.setText("");
            Airline_AirplaneRegistration.setText("");

            SelectPlane_FlightRegistration.addItem(id);
        } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_CreateButton_AirplaneRegistrationActionPerformed

    private void CreateButton_LocationRegistrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateButton_LocationRegistrationActionPerformed
        String id = AirportID_LocationRegistration.getText().trim();
        String name = AirportName_LocationRegistration.getText().trim();
        String city = AirportCity_LocationRegistration.getText().trim();
        String country = AirportCountry_LocationRegistration.getText().trim();
        String latitude = AirportLatitude_LocationRegistration.getText().trim();
        String longitude = AirportLongitude_LocationRegistration.getText().trim();

        Response<Void> response = locationController.createLocation(id, name, city, country, latitude, longitude);

        if (response.getStatus() == Status.CREATED) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

            AirportID_LocationRegistration.setText("");
            AirportName_LocationRegistration.setText("");
            AirportCity_LocationRegistration.setText("");
            AirportCountry_LocationRegistration.setText("");
            AirportLatitude_LocationRegistration.setText("");
            AirportLongitude_LocationRegistration.setText("");

            SelectDepartureLocation_FlightRegistration.addItem(id);
            SelectArrivalLocation_FlightRegistration.addItem(id);
            ScaleLocation_FlightRegistration.addItem(id);
        } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_CreateButton_LocationRegistrationActionPerformed

    private void CreateButton_FlightRegistrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateButton_FlightRegistrationActionPerformed
        try {
            String id = ID_FlightRegistration.getText().trim();
            String selectedPlaneId = (String) SelectPlane_FlightRegistration.getSelectedItem();
            String depId = (String) SelectDepartureLocation_FlightRegistration.getSelectedItem();
            String arrId = (String) SelectArrivalLocation_FlightRegistration.getSelectedItem();
            String selectedScale = (String) ScaleLocation_FlightRegistration.getSelectedItem();

            int year = Integer.parseInt(DepartureDate_Day_FlightRegistration.getText());
            int month = Integer.parseInt((String) DepartureDate_SelectMonth_FlightRegistration.getSelectedItem());
            int day = Integer.parseInt((String) DepartureDate_SelectDay_FlightRegistration.getSelectedItem());
            int hour = Integer.parseInt((String) DepartureDate_SelectHour_FlightRegistration.getSelectedItem());
            int minute = Integer.parseInt((String) DepartureDate_SelectMinute_FlightRegistration.getSelectedItem());
            LocalDateTime departureDate = LocalDateTime.of(year, month, day, hour, minute);

            int durationHours = Integer.parseInt((String) ArrivalDuration_SelectHour_FlightRegistration.getSelectedItem());
            int durationMinutes = Integer.parseInt((String) ArrivalDuration_SelectMinute_FlightRegistration.getSelectedItem());

            int scaleHours = 0, scaleMinutes = 0;
            if (!"Ninguna".equals(selectedScale)) {
                scaleHours = Integer.parseInt((String) ScaleDuration_SelectHour_FlightRegistration.getSelectedItem());
                scaleMinutes = Integer.parseInt((String) ScaleDuration_SelectMinute_FlightRegistration.getSelectedItem());
            }

            Response<Void> response = flightController.createFlight(
                    id, selectedPlaneId, depId, arrId, selectedScale,
                    departureDate, durationHours, durationMinutes,
                    scaleHours, scaleMinutes
            );

            if (response.getStatus() == Status.CREATED) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                SelectFlight_AddToFlight.addItem(id);
            } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error de formato numérico: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_CreateButton_FlightRegistrationActionPerformed

    private void UpdateButton_UpdateInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateButton_UpdateInfoActionPerformed
        try {
            long id = Long.parseLong(ID_UpdateInfo.getText().trim());
            String firstname = FirstName_UpdateInfo.getText().trim();
            String lastname = LastName_UpdateInfo.getText().trim();
            String year = BirthdayYear_UpdateInfo.getText().trim();
            String month = (String) SelectBirthdayMonth_UpdateInfo.getSelectedItem();
            String day = (String) SelectBirthdayDay_UpdateInfo.getSelectedItem();
            String phoneCode = CountryIdentifier_UpdateInfo.getText().trim();
            String phone = Phone_UpdateInfo.getText().trim();
            String country = Country_UpdateInfo.getText().trim();

            Response<Void> response = passengerController.updatePassenger(
                    id, firstname, lastname, year, month, day, phoneCode, phone, country);

            if (response.getStatus() == Status.OK) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                refrescarVuelosDePasajero(id);
                cargarUsuariosEnComboBox();
            } else if (response.getStatus() == Status.NO_CONTENT) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Sin cambios", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_UpdateButton_UpdateInfoActionPerformed

    private void Add_AddToFlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Add_AddToFlightActionPerformed
        try {
            long passengerId = Long.parseLong(ID_AddToFlight.getText().trim());
            String flightId = (String) SelectFlight_AddToFlight.getSelectedItem();

            if (flightId == null || flightId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debes seleccionar un vuelo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Response<Void> response = flightController.addPassengerToFlight(passengerId, flightId);

            if (response.getStatus() == Status.OK) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                refrescarVuelosDePasajero(passengerId); // ✅ Refresca la tabla correctamente
            } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID del pasajero debe ser numérico.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_Add_AddToFlightActionPerformed

    private void DelayButton_DelayFlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelayButton_DelayFlightActionPerformed
        try {
            String flightId = (String) SelectID_DelayFlight.getSelectedItem();
            if (flightId == null || flightId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debes seleccionar un vuelo", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String hourStr = (String) SelectHour_DelayFlight.getSelectedItem();
            String minStr = (String) SelectMinute_DelayFlight.getSelectedItem();

            if ("Hour".equals(hourStr) || "Minute".equals(minStr)) {
                JOptionPane.showMessageDialog(this, "Selecciona una hora y minutos válidos para el retraso", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int hours = Integer.parseInt(hourStr);
            int minutes = Integer.parseInt(minStr);

            Response<Void> response = flightController.delayFlight(flightId, hours, minutes);

            if (response.getStatus() == Status.OK) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                RefreshButton_ShowAllFlightsActionPerformed(null);
            } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Formato inválido de número: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_DelayButton_DelayFlightActionPerformed

    private void RefreshButton_ShowAllMyFlightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshButton_ShowAllMyFlightsActionPerformed
        String selectedName = (String) userSelect.getSelectedItem();
        if (selectedName == null || selectedName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long id = nombreIdMap.get(selectedName);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el ID del usuario", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Response<List<Flight>> response = flightController.getFlightsByPassengerId(id);

        if (response.getStatus() != Status.OK) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) Table_ShowMyFlights.getModel();
        model.setRowCount(0);

        for (Flight flight : response.getObject()) {
            model.addRow(new Object[]{
                flight.getId(),
                flight.getDepartureDate(),
                flight.calculateArrivalDate()
            });
        }
    }//GEN-LAST:event_RefreshButton_ShowAllMyFlightsActionPerformed

    private void RefreshButton_ShowAllPassengersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshButton_ShowAllPassengersActionPerformed
        Response<List<Passenger>> response = passengerController.getAllPassengers();

        if (response.getStatus() == Status.OK) {
            List<Passenger> passengerList = response.getObject();
            DefaultTableModel model = (DefaultTableModel) Table_ShowAllPassengers.getModel();
            model.setRowCount(0); // Limpiar tabla

            for (Passenger passenger : passengerList) {
                model.addRow(new Object[]{
                    passenger.getId(),
                    passenger.getFullname(),
                    passenger.getBirthDate(),
                    passenger.calculateAge(),
                    passenger.generateFullPhone(),
                    passenger.getCountry(),
                    passenger.getNumFlights()
                });
            }

            if (passengerList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay pasajeros registrados.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_RefreshButton_ShowAllPassengersActionPerformed

    private void RefreshButton_ShowAllFlightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshButton_ShowAllFlightsActionPerformed
        Response<List<Flight>> response = flightController.getAllFlights();

        if (response.getStatus() == Status.OK) {
            List<Flight> flightList = response.getObject();
            DefaultTableModel model = (DefaultTableModel) Table_ShowAllFlights.getModel();
            model.setRowCount(0); // Limpiar tabla

            for (Flight flight : flightList) {
                model.addRow(new Object[]{
                    flight.getId(),
                    flight.getDepartureLocation().getAirportId(),
                    flight.getArrivalLocation().getAirportId(),
                    (flight.getScaleLocation() == null ? "-" : flight.getScaleLocation().getAirportId()),
                    flight.getDepartureDate(),
                    flight.calculateArrivalDate(),
                    flight.getPlane().getId(),
                    flight.getNumPassengers()
                });
            }

            if (flightList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay vuelos registrados.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_RefreshButton_ShowAllFlightsActionPerformed

    private void RefreshButton_ShowAllPlanesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshButton_ShowAllPlanesActionPerformed
        Response<List<Plane>> response = planeController.getAllPlanes();

        if (response.getStatus() == Status.OK) {
            List<Plane> planeList = response.getObject();
            DefaultTableModel model = (DefaultTableModel) Table_ShowAllPlanes.getModel();
            model.setRowCount(0); // Limpiar tabla

            for (Plane plane : planeList) {
                model.addRow(new Object[]{
                    plane.getId(),
                    plane.getBrand(),
                    plane.getModel(),
                    plane.getMaxCapacity(),
                    plane.getAirline(),
                    plane.getNumFlights()
                });
            }

            if (planeList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay aviones registrados.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_RefreshButton_ShowAllPlanesActionPerformed

    private void RefreshButton_ShowAllLocationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshButton_ShowAllLocationsActionPerformed
        Response<List<Location>> response = locationController.getAllLocations();

        if (response.getStatus() == Status.OK) {
            List<Location> locationList = response.getObject();
            DefaultTableModel model = (DefaultTableModel) Table_ShowAllLocations.getModel();
            model.setRowCount(0); // Limpiar tabla

            for (Location location : locationList) {
                model.addRow(new Object[]{
                    location.getAirportId(),
                    location.getAirportName(),
                    location.getAirportCity(),
                    location.getAirportCountry()
                });
            }

            if (locationList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay localizaciones registradas.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_RefreshButton_ShowAllLocationsActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void userSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userSelectActionPerformed
        String selectedName = (String) userSelect.getSelectedItem();
        if (selectedName == null || !nombreIdMap.containsKey(selectedName)) {
            return;
        }

        long id = nombreIdMap.get(selectedName);
        Response<Passenger> response = passengerController.getPassengerById(id);

        if (response.getStatus() == Status.OK && response.getObject() != null) {
            Passenger p = response.getObject();

            ID_UpdateInfo.setText(String.valueOf(p.getId()));
            ID_UpdateInfo.setEditable(false);
            FirstName_UpdateInfo.setText(p.getFirstname());
            LastName_UpdateInfo.setText(p.getLastname());
            BirthdayYear_UpdateInfo.setText(String.valueOf(p.getBirthDate().getYear()));
            SelectBirthdayMonth_UpdateInfo.setSelectedItem(String.valueOf(p.getBirthDate().getMonthValue()));
            SelectBirthdayDay_UpdateInfo.setSelectedItem(String.valueOf(p.getBirthDate().getDayOfMonth()));
            CountryIdentifier_UpdateInfo.setText(String.valueOf(p.getCountryPhoneCode()));
            Phone_UpdateInfo.setText(String.valueOf(p.getPhone()));
            Country_UpdateInfo.setText(p.getCountry());
            ID_AddToFlight.setText(String.valueOf(id));
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_userSelectActionPerformed

    private void SelectFlight_AddToFlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectFlight_AddToFlightActionPerformed
        String selected = (String) userSelect.getSelectedItem();
        if (selected != null && nombreIdMap.containsKey(selected)) {
            long id = nombreIdMap.get(selected);
            ID_AddToFlight.setText(String.valueOf(id));
        }
    }//GEN-LAST:event_SelectFlight_AddToFlightActionPerformed

    private void AirportName_LocationRegistrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AirportName_LocationRegistrationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AirportName_LocationRegistrationActionPerformed

    private void ID_AddToFlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ID_AddToFlightActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ID_AddToFlightActionPerformed

    private void SelectID_DelayFlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectID_DelayFlightActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SelectID_DelayFlightActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Add_AddToFlight;
    private javax.swing.JRadioButton AdministratorOption_Administration;
    private javax.swing.JTextField Airline_AirplaneRegistration;
    private javax.swing.JTextField AirportCity_LocationRegistration;
    private javax.swing.JTextField AirportCountry_LocationRegistration;
    private javax.swing.JTextField AirportID_LocationRegistration;
    private javax.swing.JTextField AirportLatitude_LocationRegistration;
    private javax.swing.JTextField AirportLongitude_LocationRegistration;
    private javax.swing.JTextField AirportName_LocationRegistration;
    private javax.swing.JComboBox<String> ArrivalDuration_SelectHour_FlightRegistration;
    private javax.swing.JComboBox<String> ArrivalDuration_SelectMinute_FlightRegistration;
    private javax.swing.JTextField BirhtdayYear_PassengerRegistration;
    private javax.swing.JTextField BirthdayYear_UpdateInfo;
    private javax.swing.JTextField Brand_AirplaneRegistration;
    private javax.swing.JTextField CountryIdentifier_PassengerRegistration;
    private javax.swing.JTextField CountryIdentifier_UpdateInfo;
    private javax.swing.JTextField Country_PassengerRegistration;
    private javax.swing.JTextField Country_UpdateInfo;
    private javax.swing.JButton CreateButton_AirplaneRegistration;
    private javax.swing.JButton CreateButton_FlightRegistration;
    private javax.swing.JButton CreateButton_LocationRegistration;
    private javax.swing.JButton DelayButton_DelayFlight;
    private javax.swing.JTextField DepartureDate_Day_FlightRegistration;
    private javax.swing.JComboBox<String> DepartureDate_SelectDay_FlightRegistration;
    private javax.swing.JComboBox<String> DepartureDate_SelectHour_FlightRegistration;
    private javax.swing.JComboBox<String> DepartureDate_SelectMinute_FlightRegistration;
    private javax.swing.JComboBox<String> DepartureDate_SelectMonth_FlightRegistration;
    private javax.swing.JTextField FirstName_PassengerResgistration;
    private javax.swing.JTextField FirstName_UpdateInfo;
    private javax.swing.JTextField ID_AddToFlight;
    private javax.swing.JTextField ID_FlightRegistration;
    private javax.swing.JTextField ID_UpdateInfo;
    private javax.swing.JTextField Id_AirplaneRegistration;
    private javax.swing.JTextField Id_PassengerRegistration;
    private javax.swing.JTextField LastName_PassengerRegistration;
    private javax.swing.JTextField LastName_UpdateInfo;
    private javax.swing.JTextField MaxCapacity_AirplaneRegistration;
    private javax.swing.JTextField Model_AirplaneRegistration;
    private javax.swing.JTextField Phone_PassengerRegistration;
    private javax.swing.JTextField Phone_UpdateInfo;
    private javax.swing.JButton RefreshButton_ShowAllFlights;
    private javax.swing.JButton RefreshButton_ShowAllLocations;
    private javax.swing.JButton RefreshButton_ShowAllMyFlights;
    private javax.swing.JButton RefreshButton_ShowAllPassengers;
    private javax.swing.JButton RefreshButton_ShowAllPlanes;
    private javax.swing.JPanel RefreshButton_ShowMyFlights;
    private javax.swing.JButton RegisterButton_PassengerRegistration;
    private javax.swing.JComboBox<String> ScaleDuration_SelectHour_FlightRegistration;
    private javax.swing.JComboBox<String> ScaleDuration_SelectMinute_FlightRegistration;
    private javax.swing.JComboBox<String> ScaleLocation_FlightRegistration;
    private javax.swing.JComboBox<String> SelectArrivalLocation_FlightRegistration;
    private javax.swing.JComboBox<String> SelectBirthdayDay_PassengerRegistration;
    private javax.swing.JComboBox<String> SelectBirthdayDay_UpdateInfo;
    private javax.swing.JComboBox<String> SelectBirthdayMonth_PassengerRegistration;
    private javax.swing.JComboBox<String> SelectBirthdayMonth_UpdateInfo;
    private javax.swing.JComboBox<String> SelectDepartureLocation_FlightRegistration;
    private javax.swing.JComboBox<String> SelectFlight_AddToFlight;
    private javax.swing.JComboBox<String> SelectHour_DelayFlight;
    private javax.swing.JComboBox<String> SelectID_DelayFlight;
    private javax.swing.JComboBox<String> SelectMinute_DelayFlight;
    private javax.swing.JComboBox<String> SelectPlane_FlightRegistration;
    private javax.swing.JTable Table_ShowAllFlights;
    private javax.swing.JTable Table_ShowAllLocations;
    private javax.swing.JTable Table_ShowAllPassengers;
    private javax.swing.JTable Table_ShowAllPlanes;
    private javax.swing.JTable Table_ShowMyFlights;
    private javax.swing.JButton UpdateButton_UpdateInfo;
    private javax.swing.JRadioButton UserOption_Administration;
    private javax.swing.JButton jButton13;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private airport.PanelRound panelRound1;
    private airport.PanelRound panelRound2;
    private airport.PanelRound panelRound3;
    private javax.swing.JComboBox<String> userSelect;
    // End of variables declaration//GEN-END:variables

}
