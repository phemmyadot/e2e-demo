package com.example.shopdemo.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.shopdemo.data.User
import com.example.shopdemo.testing.TestTags

@Composable
fun ProfileScreen(user: User?, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().testTag(TestTags.Profile.SCREEN)) {
        Text("Profile", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp))
        ListItem(headlineContent = { Text("Name") }, trailingContent = { Text(user?.name ?: "-") })
        ListItem(
            headlineContent = { Text("Email") },
            trailingContent = { Text(user?.email ?: "-", modifier = Modifier.testTag(TestTags.Profile.EMAIL)) },
        )
        OutlinedButton(
            onClick = onSignOut,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag(TestTags.Profile.SIGN_OUT),
        ) { Text("Sign Out") }
    }
}
