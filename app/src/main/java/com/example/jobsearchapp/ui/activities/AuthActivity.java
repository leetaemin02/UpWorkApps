package com.example.jobsearchapp.ui.activities;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.candidate.fragments.LoginFragment;

public class AuthActivity extends BaseActivity {
    @Override
    protected int getLayoutId() { return R.layout.activity_auth; }

    @Override
    protected void initViews() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new LoginFragment())
                .commit();
    }

    @Override
    protected void initListeners() {}
}