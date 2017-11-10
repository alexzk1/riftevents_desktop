package biz.an_droid.riftevents.gui;

import biz.an_droid.riftevents.api.*;
import javafx.application.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;

import java.io.IOException;
import java.util.ArrayList;


// Java 8 code
public class Main extends Application {

    // one icon location is shared between the application tray icon and task bar icon.
    // you could also use multiple icons to allow for clean display of tray icons on hi-dpi devices.
    private static final String iconImageLoc = "https://rift.trionworlds.com/site-templates/1000/favicons/rift/favicon.ico";

    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
    private Stage stage;

    public final static EventsReader reader = new EventsReader(new VostigarActivesFilter());

    // sets up the javafx application.
    // a tray icon is setup for the icon, but the main stage remains invisible until the user
    // interacts with the tray icon.
    @Override public void start(final Stage stage)
    {
        // stores a reference to the stage.
        this.stage = stage;
        stage.setTitle("RIFT Events Tracker");
        stage.resizableProperty().setValue(Boolean.FALSE);

        stage.getIcons().add(SwingFXUtils.toFXImage(ImageLoader.loadICOFromUrlForTray(iconImageLoc), null));
        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

        try
        {
            Scene scene = new Scene(buildSimpleSceneFromCode(), 300, 600);
            stage.setScene(scene);

        } catch (IOException e)
        {
            e.printStackTrace();
            this.exit();
        }
    }


    /**
     * Sets up a system tray icon for the application.
     */
    private java.awt.TrayIcon trayIcon = null;
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            trayIcon = new java.awt.TrayIcon(ImageLoader.loadICOFromUrlForTray(iconImageLoc));
            trayIcon.setToolTip("RIFT desktop event tracker.");

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem("Show");
            openItem.addActionListener(event -> Platform.runLater(this::showStage));

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(()->{
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
        } catch (java.awt.AWTException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
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
    }

    private void exit()
    {
        java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
        if (tray!= null && trayIcon != null)
            tray.remove(trayIcon);

        Platform.exit();
    }

    private static void serverSelected(final boolean newValue, final String sn, final ArrayList<String> selected)
    {
        if (newValue)
            selected.add(sn);
        else
            selected.remove(sn);
        reader.setServers((ArrayList<String>)selected.clone());
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

        Text scenetitle = new Text("Vostigar Events Only");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, row++, 2, 1);

        grid.add(new Label("EU Servers"), 0, row);
        grid.add(new Label("US Servers"), 1, row++);

        for (int i = 0, sz = RequestEvents.getEuServers().length; i < sz; ++i)
        {
            final String sn = RequestEvents.getEuServers()[i];
            CheckBox c = new CheckBox(sn);
            c.selectedProperty().addListener((observable, oldValue, newValue) -> serverSelected(newValue, sn, selected));
            grid.add(c, 0, i + row);
        }

        for (int i = 0, sz = RequestEvents.getUsServers().length; i < sz; ++i)
        {
            final String sn = RequestEvents.getUsServers()[i];
            CheckBox c = new CheckBox(sn);
            c.selectedProperty().addListener((observable, oldValue, newValue) -> serverSelected(newValue, sn, selected));
            grid.add(c, 1, i + row);
        }

        row += Math.max(RequestEvents.getEuServers().length, RequestEvents.getUsServers().length);

        grid.add(new Label("Period (s):"), 0, ++row);

        Spinner<Integer> spinner = new Spinner<Integer>(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 60, 30));
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> reader.setPeriodSeconds(newValue));

        grid.add(spinner, 1, row++);

        reader.addListener(events -> {
             for (String server : events.keySet())
             {
                 for (ServerEvent e : events.get(server))
                 {
                     System.out.println(server+": " + e.getElapsed(RequestEvents.isEuServer(server))+", " + e.getName());
                 }
             }
        });

        return grid;
    }
}