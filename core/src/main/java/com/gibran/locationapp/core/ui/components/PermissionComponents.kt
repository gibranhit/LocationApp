package com.gibran.locationapp.core.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.gibran.locationapp.core.R
import com.gibran.locationapp.core.ui.theme.Dimens.permissionButtonIconSize
import com.gibran.locationapp.core.ui.theme.Dimens.spacingLarge
import com.gibran.locationapp.core.ui.theme.Dimens.spacingMedium
import com.gibran.locationapp.core.ui.theme.Dimens.spacingSmall
import com.gibran.locationapp.core.ui.theme.Dimens.spacingXl

@Composable
fun LocationPermissionHandler(
    onPermissionResult: (Boolean) -> Unit,
    content: @Composable (
        permissionGranted: Boolean,
        requestPermission: () -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    
    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val granted = fineLocationGranted || coarseLocationGranted
            permissionGranted = granted
            onPermissionResult(granted)
            
            if (!granted) {
                showPermissionRationale = true
            }
        }
    )
    
    // Check if permissions are already granted
    LaunchedEffect(Unit) {
        val fineLocationResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val granted = fineLocationResult == PermissionChecker.PERMISSION_GRANTED ||
                coarseLocationResult == PermissionChecker.PERMISSION_GRANTED
        permissionGranted = granted
        onPermissionResult(granted)
    }
    
    val requestPermission = {
        multiplePermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    content(permissionGranted, requestPermission)
    
    // Permission rationale dialog
    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationale = false }
        )
    }
}

@Composable
fun LocationPermissionRequestUI(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacingMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(spacingXl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = stringResource(R.string.location_icon),
            modifier = Modifier.size(spacingXl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(spacingLarge))
        Text(
            text = stringResource(R.string.location_access_required),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(spacingSmall))
        Text(
            text = stringResource(R.string.location_permission_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacingLarge))
        Button(onClick = onRequestPermission) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(permissionButtonIconSize)
            )
            Spacer(modifier = Modifier.width(spacingSmall))
            Text(stringResource(R.string.grant_location_permission))
        }
    }
}

@Composable
private fun PermissionRationaleDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.location_permission_required_dialog_title))
        },
        text = {
            Text(stringResource(R.string.location_permission_required_dialog_text))
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}