package com.example.watwallet.feature.profile.job.ui.components

import android.location.Address
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


@ExperimentalMaterial3Api
@Composable
fun LocationHandle(
    locationSearchValue: String,
    onLocationSearchChange: (value: String) -> Unit,
    selectedLocation: Address?,
    locationsSearch: List<Address>,
    onLocationSelect: (Address) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Properly handle map state to reflect location changes
    var startLocation by remember {
        mutableStateOf(selectedLocation?.let { LatLng(it.latitude, it.longitude) })
    }

    val markerState = remember {
        mutableStateOf(
            startLocation?.let { MarkerState(position = it) }
        )
    }

    val cameraPositionState = remember {
        mutableStateOf(
            startLocation?.let {
                CameraPositionState(position = CameraPosition.fromLatLngZoom(it, 10f))
            }
        )
    }

    Row {
        Button(
            modifier = Modifier
                .weight(1f)
                .height(55.dp),
            onClick = { showBottomSheet = true },
            shape = RoundedCornerShape(4.dp)
        ) {
            if (selectedLocation != null) {
                Text("${selectedLocation.featureName}, ${selectedLocation.countryName}")
            } else {
                Text("Select Location")
            }
        }
    }


    var active by rememberSaveable { mutableStateOf(false) }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = locationSearchValue,
                        onQueryChange = {
                            onLocationSearchChange(it)
                        },
                        onSearch = {},
                        expanded = active,
                        onExpandedChange = { active = it },
                        enabled = true,
                        placeholder = { Text("e.g. The Boardwalk") },
                        leadingIcon = null,
                        trailingIcon = {
                            if (!active)
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "search icon"
                                )
                            else
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "clear icon",
                                    modifier = Modifier.clickable {
                                        active = false
                                        onLocationSearchChange("")
                                    }
                                )
                        },
                        interactionSource = null,
                    )
                },
                expanded = active,
                onExpandedChange = { active = it },
                modifier = Modifier.padding(15.dp),
                shape = RoundedCornerShape(4.dp),
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = SearchBarDefaults.windowInsets
            ) {
                locationsSearch.forEach { address ->
                    ListItem(
                        headlineContent = { Text("${address.featureName}, ${address.countryName}") },
                        modifier = Modifier
                            .clickable {
                                // Update location and marker state
                                val newLatLng = LatLng(address.latitude, address.longitude)
                                startLocation = newLatLng
                                markerState.value = MarkerState(position = newLatLng)
                                cameraPositionState.value = CameraPositionState(
                                    position = CameraPosition.fromLatLngZoom(newLatLng, 5f)
                                )

                                // Inform the view model about the selected location
                                onLocationSelect(address)
                                active = false
                            }
                            .fillMaxWidth()
                    )
                }
            }

            // Show the map only if the start location is available
            if (startLocation != null && markerState.value != null && cameraPositionState.value != null && !active) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(10.dp),
                    cameraPositionState = cameraPositionState.value!!
                ) {
                    Marker(
                        state = markerState.value!!,
                        title = "Selected Location",
                        snippet = "Start Marker"
                    )
                }
            }

        }
    }
}
