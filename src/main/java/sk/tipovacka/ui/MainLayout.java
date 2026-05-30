package sk.tipovacka.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import sk.tipovacka.ui.admin.AdminView;

public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 title = new H1("Tipovačka");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, LumoUtility.Padding.SMALL);

        addToNavbar(new DrawerToggle(), title);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Pridaj tipy", GuessView.class, VaadinIcon.PENCIL.create()));
        nav.addItem(new SideNavItem("Tabuľka", LeaderboardView.class, VaadinIcon.TROPHY.create()));
        nav.addItem(new SideNavItem("Admin", AdminView.class, VaadinIcon.COG.create()));
        addToDrawer(nav);
    }
}
