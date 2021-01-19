package com.miguel.audiorecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    NavigationView mNavDrawer;
    DrawerLayout mDrawerLayout;

    NavController mNavController;
    AppBarConfiguration abc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        mNavDrawer = findViewById(R.id.nav_drawer);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavHost navHost = (NavHost) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        mNavController = navHost.getNavController();

        final int[] topDestination = {R.id.recordFragment, R.id.recordListFragment};
        AppBarConfiguration.Builder abcBuilder = new AppBarConfiguration.Builder(topDestination)
                .setOpenableLayout(mDrawerLayout);

        abc = abcBuilder.build();

        setSupportActionBar(toolbar);
        NavigationUI.setupActionBarWithNavController(this,mNavController, abc);
        NavigationUI.setupWithNavController(mNavDrawer, mNavController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp() || NavigationUI.navigateUp(mNavController, abc);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, mNavController) || super.onOptionsItemSelected(item);
    }
}