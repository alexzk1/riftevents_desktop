package biz.an_droid.riftevents.gui;

import biz.an_droid.riftevents.api.*;
import com.sun.javafx.PlatformUtil;
import javafx.application.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.TrayNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


// Java 8 code
public class Main extends Application {

    private static final Preferences prefs = Preferences.userNodeForPackage(Main.class);
    // one icon location is shared between the application tray icon and task bar icon.
    // you could also use multiple icons to allow for clean display of tray icons on hi-dpi devices.
    private static final String iconImageLoc = "https://rift.trionworlds.com/site-templates/1000/favicons/rift/favicon.ico";

    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
    private Stage stage;

    public final static EventsReaderThreaded reader = new EventsReaderThreaded(new VostigarActivesFilter(), false);

    // sets up the javafx application.
    // a tray icon is setup for the icon, but the main stage remains invisible until the user
    // interacts with the tray icon.
    @Override
    public void start(final Stage stage)
    {
        // stores a reference to the stage.
        this.stage = stage;
        stage.setTitle("RIFT Events Tracker");
        stage.resizableProperty().setValue(Boolean.FALSE);

        stage.getIcons().add(SwingFXUtils.toFXImage(ImageLoader.loadICOFromUrlForTray(iconImageLoc), null));
        // instructs the javafx system not to exit implicitly when the last application window is shut.


        try
        {
            Scene scene = new Scene(buildSimpleSceneFromCode(), 300, 600);
            stage.setScene(scene);

        } catch (IOException e)
        {
            e.printStackTrace();
            this.exit();
        }

        // sets up the tray icon (using awt code run on the swing thread).
        Platform.runLater(()->
        {
            //ok, on my lxqt icon is broken
            if (!PlatformUtil.isLinux() && addAppToTray())
                Platform.setImplicitExit(false);
            else
                showStage();
        });
    }


    /**
     * Sets up a system tray icon for the application.
     */
    private java.awt.TrayIcon trayIcon = null;
    private boolean addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (java.awt.SystemTray.isSupported())
            {


                // set up a system tray icon.
                java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                trayIcon = new java.awt.TrayIcon(ImageLoader.loadICOFromUrlForTray(iconImageLoc));
                trayIcon.setToolTip("RIFT desktop event tracker.");

                // if the user selects the default menu item (which includes the app name),
                // show the main app stage.
                java.awt.MenuItem openItem = new java.awt.MenuItem("Show");
                openItem.addActionListener(event -> Platform.runLater(this::showStage));

                // if the user double-clicks on the tray icon, show the main app stage.
                trayIcon.addActionListener(event -> Platform.runLater(() -> {
                    if (stage.isShowing())
                        stage.hide();
                    else
                        showStage();
                }));

                // the convention for tray icons seems to be to set the default icon for opening
                // the application stage in a bold font.
                java.awt.Font defaultFont = java.awt.Font.decode(null);
                java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
                openItem.setFont(boldFont);

                // to really exit the application, the user must go to the system tray icon
                // and select the exit option, this will shutdown JavaFX and remove the
                // tray icon (removing the tray icon will also shut down AWT).
                java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
                exitItem.addActionListener(event -> this.exit());

                // setup the popup menu for the application.
                final java.awt.PopupMenu popup = new java.awt.PopupMenu();
                popup.add(openItem);
                popup.addSeparator();
                popup.add(exitItem);
                trayIcon.setPopupMenu(popup);


                // add the application tray icon to the system tray.
                tray.add(trayIcon);
                return true;
            }
        } catch (java.awt.AWTException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }

    public static void main(String[] args) throws Exception
    {
        launch(args);
        reader.close();
        prefs.flush();
    }

    private void exit()
    {
        java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
        if (tray!= null && trayIcon != null)
            tray.remove(trayIcon);

        Platform.exit();
    }

    private static void serverSelected(final Preferences prefs, final boolean newValue, final String sn, final ArrayList<String> selected)
    {
        if (newValue)
            selected.add(sn);
        else
            selected.remove(sn);

        prefs.putBoolean(sn, newValue);
        reader.setServers((ArrayList<String>)selected.clone());
    }

    private final static String period_key = "PERIOD";
    private final static String popup_key = "POPUP";

    public static class TableElement
    {
        private final SimpleStringProperty  server;
        private final SimpleIntegerProperty duration;

        public TableElement(String server, int duration)
        {
            this.server = new SimpleStringProperty(server);
            this.duration = new SimpleIntegerProperty(duration);
        }

        public String getServer()
        {
            return server.get();
        }

        public void setServer(String newValue)
        {
            server.set(newValue);
        }

        public int getDuration()
        {
            return duration.get();
        }

        public void setDuration(int newValue)
        {
            duration.set(newValue);
        }
    }

    private Parent buildSimpleSceneFromCode() throws IOException
    {
        // FIXME: 11/10/17 :make nice gui later
        //FXMLLoader.load(ResourceLoader.getResource("mainform.fxml"));
        
        GridPane grid = new GridPane();
        final ArrayList<String> selected = new ArrayList<>(20);

        int row = 0;
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        //wana read timing ASAP and set it to thread, because it will be sleeping initially
        Spinner<Integer> spinner = new Spinner<Integer>(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 120, prefs.getInt(period_key, 30)));
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            prefs.putInt(period_key, newValue);
            reader.setPeriodSeconds(newValue);
        });
        reader.setPeriodSeconds(spinner.getValue());


        Text scenetitle = new Text("Vostigar Events Only");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, row++, 2, 1);

        grid.add(new Label("EU Servers"), 0, row);
        grid.add(new Label("US Servers"), 1, row++);

        for (int i = 0, sz = RequestEvents.getEuServers().length; i < sz; ++i)
        {
            final String sn = RequestEvents.getEuServers()[i];
            CheckBox c = new CheckBox(sn);
            c.setSelected(prefs.getBoolean(sn, true));
            c.selectedProperty().addListener((observable, oldValue, newValue) -> serverSelected(prefs, newValue, sn, selected));
            grid.add(c, 0, i + row);
            if (c.isSelected())
                selected.add(sn);
        }

        for (int i = 0, sz = RequestEvents.getUsServers().length; i < sz; ++i)
        {
            final String sn = RequestEvents.getUsServers()[i];
            CheckBox c = new CheckBox(sn);
            c.setSelected(prefs.getBoolean(sn, false));//yeh, I play on EU server
            c.selectedProperty().addListener((observable, oldValue, newValue) -> serverSelected(prefs, newValue, sn, selected));
            grid.add(c, 1, i + row);
            if (c.isSelected())
                selected.add(sn);
        }
        reader.setServers(selected);

        row += Math.max(RequestEvents.getEuServers().length, RequestEvents.getUsServers().length);
        grid.add(new Label("Period (s):"), 0, ++row);
        grid.add(spinner, 1, row++);

        final CheckBox cp = new CheckBox("Show popup on event started.");
        grid.add(cp, 0, row++,2,1);

        cp.setSelected(prefs.getBoolean(popup_key, true));
        cp.selectedProperty().addListener((observable, oldValue, newValue) -> prefs.putBoolean(popup_key, newValue));

        final TableView table = new TableView();
        grid.add(table, 0, row, 2, 10);
        row += 10;

        final TableColumn colServer   = new TableColumn<>("Server");
        final TableColumn colDuration = new TableColumn<>("Duration(min)");
        table.getColumns().addAll(colServer, colDuration);

        colServer.setCellValueFactory(new PropertyValueFactory<TableElement,String>("server"));
        colDuration.setCellValueFactory(new PropertyValueFactory<TableElement,Integer>("duration"));
        table.setColumnResizePolicy(param -> true); //automatic resize
        
        colServer.setSortType(TableColumn.SortType.ASCENDING);
        colDuration.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().clear();
        table.getSortOrder().addAll(colDuration, colServer);
        
        final TrayNotification tray = new TrayNotification();
        tray.setTitle("New RIFT Event(s)");
        tray.setImage(SwingFXUtils.toFXImage(ImageLoader.loadICOFromUrlForTray(iconImageLoc), null));
        tray.setMessage("Double click icon to see events table.");

        tray.setAnimationType(AnimationType.FADE);
        reader.addListener((events, new_events) -> {
            final ObservableList<TableElement> data = FXCollections.observableArrayList();

            if (events != null && !events.isEmpty())
            {
                for (String server : events.keySet())
                {
                    for (ServerEvent e : events.get(server))
                    {
                        data.add(new TableElement(server, (int)e.getElapsedSeconds(RequestEvents.isEuServer(server))/60));
                    }
                }
            }
            Platform.runLater(()->{
                table.setItems(data);
                
                if (new_events && cp.isSelected())
                {
                    tray.setMessage("Double click icon to see events table.");
                    tray.showAndDismiss(Duration.seconds(3));
                }

            });
            System.gc();
        });

        reader.start();

        return grid;
    }
}