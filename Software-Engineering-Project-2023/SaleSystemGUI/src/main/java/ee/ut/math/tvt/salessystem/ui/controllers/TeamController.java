package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.TeamInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import java.net.URL;
import java.util.ResourceBundle;

public class TeamController implements Initializable {

    @FXML
    private Text name;

    @FXML
    private Text contact;

    @FXML
    private Text members;
    @FXML
    private ImageView logo;

    private ResourceBundle bundle;

    private final SalesSystemDAO dao;

    public TeamController(SalesSystemDAO dao) {
        this.dao = dao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TeamInfo team = new TeamInfo();
        name.setText(team.teamInfo()[0]);
        contact.setText(team.teamInfo()[1]);
        members.setText(team.teamInfo()[2]);
        logo.setImage(new Image(team.teamInfo()[3]));
    }
    public void addTeamMember(String member) {
        String currentMembers = members.getText();
        currentMembers += ", " + member;
        members.setText(currentMembers);
    }

    public void deleteTeamMember(String member) {
        String currentMembers = members.getText();
        currentMembers = currentMembers.replace(", " + member, "");
        members.setText(currentMembers);
    }
}
