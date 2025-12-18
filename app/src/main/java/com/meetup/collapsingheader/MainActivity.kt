package com.meetup.collapsingheader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetup.collapsingheader.ui.theme.CollapsingHeaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollapsingHeaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    CollectionScreen()
                }
            }
        }
    }
}

// Data class for card items
data class CardItem(
    val id: Int,
    val name: String,
    val setCode: String,
    val setNumber: String,
    val condition: String,
    val language: String,
    val rarity: String,
    val imageColor: Color,
    var quantity: Int = 1
)

// Sample data
val sampleCards = listOf(
    CardItem(1, "#First_Step, Kyouka", "D-LBT03", "D-LBT03/097EN", "NM", "EN", "Common", Color(0xFF7CB9E8)),
    CardItem(2, "Katara, Water Tribe's Hope Art Card (Gold-Stamped Signature)", "ASTLA", "48", "NM", "EN", "Special", Color(0xFF6B8E23)),
    CardItem(3, "Apprentice Illusion Magician (JMPS-EN007)", "SJMP", "JMPS-EN007", "NM", "EN", "Ultra Rare", Color(0xFF9370DB)),
    CardItem(4, "Blue-Eyes White Dragon (JMP-001)", "SJMP", "JMP-001", "NM", "EN", "Unlimited", Color(0xFF4169E1)),
    CardItem(5, "Dark Magician (LC01-EN005)", "LC01", "LC01-EN005", "NM", "EN", "Ultra Rare", Color(0xFF8B008B)),
    CardItem(6, "Red-Eyes Black Dragon (LDK2-ENJ01)", "LDK2", "LDK2-ENJ01", "NM", "EN", "Common", Color(0xFF8B0000)),
    CardItem(7, "Exodia the Forbidden One (LOB-124)", "LOB", "LOB-124", "NM", "EN", "Ultra Rare", Color(0xFFDAA520)),
    CardItem(8, "Pot of Greed (LOB-119)", "LOB", "LOB-119", "NM", "EN", "Rare", Color(0xFF228B22)),
    CardItem(9, "Monster Reborn (LOB-118)", "LOB", "LOB-118", "NM", "EN", "Ultra Rare", Color(0xFF00CED1)),
    CardItem(10, "Mirror Force (MRD-138)", "MRD", "MRD-138", "NM", "EN", "Ultra Rare", Color(0xFFFF69B4)),
    CardItem(11, "Raigeki (LOB-053)", "LOB", "LOB-053", "NM", "EN", "Super Rare", Color(0xFFFFD700)),
    CardItem(12, "Change of Heart (MRD-060)", "MRD", "MRD-060", "NM", "EN", "Ultra Rare", Color(0xFFFF6347)),
    CardItem(13, "Cyber Dragon (CRV-EN015)", "CRV", "CRV-EN015", "NM", "EN", "Ultra Rare", Color(0xFFC0C0C0)),
    CardItem(14, "Stardust Dragon (TDGS-EN040)", "TDGS", "TDGS-EN040", "NM", "EN", "Ultra Rare", Color(0xFFE6E6FA))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen() {
    val listState = rememberLazyListState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter cards based on search query
    val filteredCards = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            sampleCards
        } else {
            sampleCards.filter { card ->
                card.name.contains(searchQuery, ignoreCase = true) ||
                card.setCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Calculate collapse progress based on scroll (only when search is not active)
    val scrollOffset by remember {
        derivedStateOf {
            if (isSearchActive) {
                1f // Fully collapsed when search is active
            } else if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f)
            }
        }
    }
    
    // Animate the collapse
    val collapseProgress by animateFloatAsState(
        targetValue = scrollOffset,
        animationSpec = tween(durationMillis = 150),
        label = "collapse"
    )
    
    // Header height animation
    val headerHeight by animateDpAsState(
        targetValue = if (isSearchActive) 0.dp else (120 - (collapseProgress * 90)).dp,
        animationSpec = tween(durationMillis = 150),
        label = "headerHeight"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Toolbar with Search - Always visible
        TopToolbar(
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchClick = { isSearchActive = true },
            onSearchQueryChange = { searchQuery = it },
            onCloseSearch = { 
                isSearchActive = false
                searchQuery = ""
            }
        )
        
        // Collapsible Header Content - Hidden when search is active
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CollapsibleHeader(
                collapseProgress = collapseProgress,
                headerHeight = headerHeight
            )
        }
        
        // Filter/Sort Bar - Always visible
        FilterSortBar()
        
        // Divider
        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        
        // Card List
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredCards) { card ->
                CardListItem(card = card)
                HorizontalDivider(
                    color = Color(0xFFF0F0F0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(start = 80.dp)
                )
            }
        }
    }
}

@Composable
fun TopToolbar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button / Close search
        IconButton(onClick = { 
            if (isSearchActive) {
                onCloseSearch()
                focusManager.clearFocus()
            }
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        
        if (isSearchActive) {
            // Search input field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search cards...",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Color.Black)
                )
            }
            
            // Clear search button
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
        } else {
            // Normal toolbar content
            Text(
                text = "—",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action icons
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Black
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.Black
                )
            }
            
            IconButton(onClick = { }) {
                Text(
                    text = "‹",
                    color = Color.Black,
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun CollapsibleHeader(
    collapseProgress: Float,
    headerHeight: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .graphicsLayer {
                alpha = 1f - collapseProgress
            }
            .padding(horizontal = 16.dp)
    ) {
        // Category and count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.alpha(1f - collapseProgress)
        ) {
            Text(
                text = "Standard",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "  •  ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "14 items",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Title
        Text(
            text = "Title_Name",
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alpha(1f - collapseProgress)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Price estimate
        Text(
            text = "$146.42 (Est. Market Price)",
            color = Color(0xFF4CAF50),
            fontSize = 14.sp,
            modifier = Modifier.alpha(1f - collapseProgress)
        )
    }
}

@Composable
fun FilterSortBar() {
    var selectedView by remember { mutableIntStateOf(3) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sort button
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort",
                color = Color.Black,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "↕",
                color = Color.Black,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // View options
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { selectedView = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙",
                    color = if (selectedView == 0) Color.Black else Color.Gray,
                    fontSize = 20.sp
                )
            }
            
            // Small grid
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { selectedView = 1 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▦",
                    color = if (selectedView == 1) Color.Black else Color.Gray,
                    fontSize = 20.sp
                )
            }
            
            // Large grid
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { selectedView = 2 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⊞",
                    color = if (selectedView == 2) Color.Black else Color.Gray,
                    fontSize = 20.sp
                )
            }
            
            // List view
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { selectedView = 3 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☰",
                    color = if (selectedView == 3) Color.Black else Color.Gray,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun CardListItem(card: CardItem) {
    var quantity by remember { mutableIntStateOf(card.quantity) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Card image placeholder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(card.imageColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🃏",
                fontSize = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Card details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Card name
            Text(
                text = card.name,
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Card details row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.setCode,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = ", ${card.setNumber}, ",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Text(
                    text = card.condition,
                    color = Color(0xFF4CAF50),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = ", ${card.language}, ",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Text(
                    text = card.rarity,
                    color = if (card.rarity == "Limited" || card.rarity == "Unlimited") 
                        Color(0xFFE91E63) else Color.Gray,
                    fontSize = 13.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick Edit and quantity controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Edit link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Quick Edit",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "↗",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Minus button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable { if (quantity > 0) quantity-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "−",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Quantity display with dropdown
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
                        Text(
                            text = quantity.toString(),
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select quantity",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Plus button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable { quantity++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionScreenPreview() {
    CollapsingHeaderTheme {
        CollectionScreen()
    }
}
