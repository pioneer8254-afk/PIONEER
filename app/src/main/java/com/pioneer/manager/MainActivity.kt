package com.pioneer.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF08131F)
private val Panel = Color(0xFF122334)
private val PanelLight = Color(0xFF1B3042)
private val Mint = Color(0xFF35D39A)
private val Muted = Color(0xFF9BB0BF)
private val White = Color(0xFFF4F7FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PioneerApp() }
    }
}

@Composable
fun PioneerApp() {
    val context = LocalContext.current
    var save by remember { mutableStateOf(GameStore.load(context)) }
    var activeTab by rememberSaveable { mutableStateOf("home") }

    LaunchedEffect(save?.lastTickEpochMillis) {
        val current = save ?: return@LaunchedEffect
        val updated = GameStore.advanceElapsedDays(current)
        if (updated != current) {
            GameStore.save(context, updated)
            save = updated
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = darkColorScheme(
            primary = Mint, secondary = Color(0xFF79B8FF), background = Navy,
            surface = Panel, onPrimary = Navy, onBackground = White, onSurface = White
        )) {
            Surface(modifier = Modifier.fillMaxSize(), color = Navy) {
                if (save == null) {
                    ClubPicker(onSelect = { club ->
                        save = GameStore.startNewGame(context, club)
                        activeTab = "home"
                    })
                } else {
                    MainDashboard(
                        save = save!!,
                        activeTab = activeTab,
                        onTabChange = { activeTab = it },
                        onSaveChange = { updated ->
                            GameStore.save(context, updated)
                            save = updated
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClubPicker(onSelect: (Club) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text("PIONEER", color = Mint, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("مدیریت باشگاه فوتبال", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("باشگاه خود را برای شروع فصل 2026/27 انتخاب کن.", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("20 باشگاه", color = Mint, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text("Premier League", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(ClubCatalog.premierLeague) { club ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { onSelect(club) },
                            colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
                        ) { Text("انتخاب") }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(club.name, color = White, fontWeight = FontWeight.Bold)
                            Text("بودجه آغازین: €" + club.initialBudgetMillions + "M", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainDashboard(save: GameSave, activeTab: String, onTabChange: (String) -> Unit, onSaveChange: (GameSave) -> Unit) {
    val tabs = listOf(
        "home" to "خانه", "squad" to "ترکیب", "fixtures" to "مسابقات", "table" to "جدول",
        "tactics" to "تاکتیک", "managers" to "مربیان", "market" to "بازار", "upgrade" to "ارتقا", "honors" to "افتخارات",
        "finance" to "مالی", "news" to "اخبار", "settings" to "تنظیمات"
    )
    val activeAccent = when (activeTab) {
        "home" -> Color(0xFF35D39A)
        "squad" -> Color(0xFF71D6A2)
        "fixtures" -> Color(0xFFFF9B68)
        "table" -> Color(0xFF68B9FF)
        "tactics" -> Color(0xFFB69AFF)
        "managers" -> Color(0xFFCE9BFF)
        "market" -> Color(0xFF52D7D0)
        "upgrade" -> Color(0xFFFFD166)
        "honors" -> Color(0xFFE8B85C)
        "finance" -> Color(0xFF8FD18A)
        "news" -> Color(0xFF8EC8F5)
        else -> Color(0xFF9BB0BF)
    }
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(top = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PIONEER", color = Mint, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Column(horizontalAlignment = Alignment.End) {
                Text(save.clubName, color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("SEASON " + save.season + "  •  DAY " + save.gameDay, color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
        ) {
            items(tabs) { item ->
                if (activeTab == item.first) {
                    Button(
                        onClick = { onTabChange(item.first) },
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent, contentColor = Navy)
                    ) { Text(item.second, fontWeight = FontWeight.Bold) }
                } else {
                    OutlinedButton(onClick = { onTabChange(item.first) }) { Text(item.second, color = Muted) }
                }
            }
        }
        Divider(color = activeAccent.copy(alpha = 0.65f), thickness = 1.dp)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        when (activeTab) {
            "home" -> HomeScreen(save)
            "squad" -> InfoScreen("ترکیب تیم", "بانک بازیکنان واقعی فصل 2026/27 و کارت‌های شش‌ضلعی در گام بعدی متصل می‌شود.")
            "fixtures" -> FixturesScreen(save)
            "table" -> LeagueTableScreen(save)
            "tactics" -> TacticsScreen(save, onSaveChange)
            "managers" -> ManagerStoreScreen(save, onSaveChange)
            "market" -> InfoScreen("نقل‌وانتقالات", "بازار خرید، فروش و معاوضه با سقف‌های روزانه طبق قوانین بازی در دست ساخت است.")
            "upgrade" -> InfoScreen("ارتقای بازیکنان", "سیستم امتیاز ارتقا و انتخاب ویژگی اختصاصی یا خودکار در مرحله بعد اضافه می‌شود.")
            "honors" -> InfoScreen("افتخارات", "جام‌ها و جوایز پایان فصل در این بخش ثبت خواهند شد.")
            "finance" -> FinanceScreen(save, onSaveChange)
            "news" -> InfoScreen("اخبار باشگاه", "رویدادهای مهم باشگاه و اعلان‌ها در این بخش نمایش داده می‌شوند.")
            "settings" -> SettingsScreen(save) { newName ->
                onSaveChange(save.copy(clubName = newName.trim()))
            }
            else -> HomeScreen(save)
        }
        }
    }
}

@Composable
private fun HomeScreen(save: GameSave) {
    val clubFixtures = save.fixtures
        .filter { it.homeClubId == save.clubId || it.awayClubId == save.clubId }
        .sortedBy { it.matchday }
    val played = clubFixtures.filter { it.played }
    val upcoming = clubFixtures.firstOrNull { !it.played }
    val wins = played.count { fixture ->
        val home = fixture.homeClubId == save.clubId
        val scored = if (home) fixture.homeGoals else fixture.awayGoals
        val conceded = if (home) fixture.awayGoals else fixture.homeGoals
        scored != null && conceded != null && scored > conceded
    }
    val opponentId = upcoming?.let {
        if (it.homeClubId == save.clubId) it.awayClubId else it.homeClubId
    }
    val opponentName = opponentId?.let { ClubCatalog.find(it)?.name } ?: "نامشخص"

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF102B31)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PIONEER  /  CLUB PROFILE", color = Color(0xFF75E7C0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("●  ACTIVE CLUB", color = Color(0xFF75E7C0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(save.clubName, color = White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("مدیریت باشگاه  •  فصل ${save.season}", color = Color(0xFFB8D7D0), fontSize = 12.sp)
                Divider(color = Color(0xFF31564F))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("موجودی قابل استفاده", color = Color(0xFFB8D7D0), fontSize = 11.sp)
                        Text("€" + "%,d".format(save.budget), color = Color(0xFF75E7C0), fontSize = 25.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("سرمربی", color = Color(0xFFB8D7D0), fontSize = 11.sp)
                        Text(save.managerName, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            StatCard("بازیکنان", save.players.size.toString(), Modifier.weight(1f), Color(0xFF63B3FF))
            StatCard("مسابقات", played.size.toString() + " / " + clubFixtures.size, Modifier.weight(1f), Color(0xFFFFB45C))
            StatCard("پیروزی", wins.toString(), Modifier.weight(1f), Color(0xFF75E7C0))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("NEXT FIXTURE", color = Color(0xFFFFB45C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("مسابقه پیش‌رو", color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                if (upcoming == null) {
                    Text("در حال حاضر مسابقه برنامه‌ریزی‌شده‌ای باقی نمانده است.", color = Muted, fontSize = 13.sp)
                } else {
                    Text(opponentName, color = White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Text(
                        (if (upcoming.homeClubId == save.clubId) "بازی خانگی" else "بازی خارج از خانه") +
                            "  •  هفته ${upcoming.matchday}",
                        color = Muted, fontSize = 12.sp
                    )
                    Text("وضعیت: در انتظار برگزاری", color = Color(0xFFFFB45C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PanelLight),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("گزارش مدیریتی", color = Color(0xFFBCA7FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("وضعیت تیم", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "ترکیب فعلی: ${save.players.size} بازیکن  •  آرایش: ${save.formation}",
                    color = Muted, fontSize = 12.sp
                )
                Text(
                    "اطلاعات بازیکنان و لوگوهای رسمی باید از منابع معتبر تکمیل شوند؛ تا پیش از تأیید، نشان یا اطلاعات ساختگی نمایش داده نمی‌شود.",
                    color = White, fontSize = 12.sp, lineHeight = 19.sp
                )
            }
        }
        Text("PIONEER  •  FOOTBALL MANAGEMENT", color = Color(0xFF607887), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Mint
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Text(value, color = accent, fontSize = 19.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun InfoScreen(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = Mint, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(message, color = Muted, fontSize = 15.sp, lineHeight = 25.sp)
    }
}

@Composable
private fun SettingsScreen(save: GameSave, onRename: (String) -> Unit) {
    var newName by rememberSaveable(save.clubName) { mutableStateOf(save.clubName) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("تنظیمات باشگاه", color = Mint, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("نام باشگاه", color = Muted)
        OutlinedTextField(
            value = newName, onValueChange = { newName = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("نام جدید") }
        )
        Button(
            onClick = { if (newName.isNotBlank()) onRename(newName) },
            colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
        ) { Text("ذخیره نام") }
        Spacer(Modifier.height(18.dp))
        Text("ذخیره بازی به‌صورت خودکار انجام می‌شود.", color = Muted, fontSize = 13.sp)
    }
}


@Composable
private fun TacticsScreen(save: GameSave, onSaveChange: (GameSave) -> Unit) {
    val formations = listOf("4-3-3", "4-2-3-1", "4-4-2", "3-5-2", "3-4-3", "5-3-2", "4-1-4-1")
    val styles = listOf(
        "Balanced" to "متعادل",
        "Possession" to "مالکانه",
        "Counter" to "ضدحمله",
        "High Press" to "پرس شدید",
        "Defensive" to "دفاعی"
    )
    val tactics = save.tactics
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("تاکتیک و آرایش تیم", color = Mint, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("انتخاب آرایش", color = Muted, fontSize = 13.sp)
        formations.chunked(3).forEach { group ->
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                group.forEach { formation ->
                    if (save.formation == formation) {
                        Button(
                            onClick = { onSaveChange(save.copy(formation = formation)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
                        ) { Text(formation) }
                    } else {
                        OutlinedButton(onClick = { onSaveChange(save.copy(formation = formation)) }) {
                            Text(formation, color = Muted)
                        }
                    }
                }
            }
        }
        Divider(color = PanelLight)
        Text("سبک بازی", color = Muted, fontSize = 13.sp)
        styles.forEach { (style, label) ->
            if (tactics.style == style) {
                Button(
                    onClick = { onSaveChange(save.copy(tactics = tactics.copy(style = style))) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
                ) { Text(label) }
            } else {
                OutlinedButton(
                    onClick = { onSaveChange(save.copy(tactics = tactics.copy(style = style))) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(label, color = White) }
            }
        }
        TacticSlider("شدت پرس", tactics.pressing) {
            onSaveChange(save.copy(tactics = tactics.copy(pressing = it)))
        }
        TacticSlider("ارتفاع خط دفاع", tactics.defensiveLine) {
            onSaveChange(save.copy(tactics = tactics.copy(defensiveLine = it)))
        }
        TacticSlider("سرعت بازی", tactics.tempo) {
            onSaveChange(save.copy(tactics = tactics.copy(tempo = it)))
        }
        TacticSlider("عرض بازی", tactics.width) {
            onSaveChange(save.copy(tactics = tactics.copy(width = it)))
        }
        Text("تغییرات تاکتیکی به‌صورت خودکار ذخیره می‌شوند.", color = Mint, fontSize = 12.sp)
    }
}

@Composable
private fun TacticSlider(title: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(value.toString(), color = Mint, fontWeight = FontWeight.Bold)
            Text(title, color = White, fontSize = 14.sp)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f
        )
    }
}


@Composable
private fun FixturesScreen(save: GameSave) {
    val clubFixtures = save.fixtures
        .filter { it.homeClubId == save.clubId || it.awayClubId == save.clubId }
        .sortedBy { it.matchday }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.End) {
        Text("تقویم لیگ", color = Mint, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("38 مسابقه لیگ برتر • بازی‌های انجام‌نشده در تقویم باقی می‌مانند.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(clubFixtures) { fixture ->
                val isHome = fixture.homeClubId == save.clubId
                val opponentId = if (isHome) fixture.awayClubId else fixture.homeClubId
                val opponent = ClubCatalog.find(opponentId)?.name ?: opponentId
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (fixture.played) "پایان‌یافته" else "در انتظار", color = if (fixture.played) Mint else Muted, fontSize = 11.sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("هفته " + fixture.matchday, color = Mint, fontSize = 11.sp)
                            Text(if (isHome) "میزبان • " + opponent else "میهمان • " + opponent, color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueTableScreen(save: GameSave) {
    val clubIds = ClubCatalog.premierLeague.map { it.id }
    val standings = LeagueEngine.calculateStandings(clubIds, save.fixtures)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), horizontalAlignment = Alignment.End) {
        Text("جدول Premier League", color = Mint, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("امتیازدهی: برد 3 • مساوی 1 • باخت 0", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.End
        ) {
            Column {
                Row(modifier = Modifier.padding(vertical = 7.dp)) {
                    listOf("PTS", "GD", "GA", "GF", "L", "D", "W", "P", "CLUB", "#").forEach {
                        Text(it, color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
                    }
                }
                standings.forEachIndexed { index, standing ->
                    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        val values = listOf(
                            standing.points.toString(), standing.goalDifference.toString(),
                            standing.goalsAgainst.toString(), standing.goalsFor.toString(),
                            standing.losses.toString(), standing.draws.toString(),
                            standing.wins.toString(), standing.played.toString(),
                            ClubCatalog.find(standing.clubId)?.name ?: standing.clubId,
                            (index + 1).toString()
                        )
                        values.forEachIndexed { valueIndex, value ->
                            Text(
                                value,
                                color = if (valueIndex == 0) Mint else White,
                                fontSize = if (valueIndex == 8) 10.sp else 11.sp,
                                fontWeight = if (valueIndex == 0 || valueIndex == 8) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.width(if (valueIndex == 8) 142.dp else 48.dp)
                            )
                        }
                    }
                    Divider(color = PanelLight)
                }
            }
        }
    }
}


@Composable
private fun ManagerStoreScreen(save: GameSave, onSaveChange: (GameSave) -> Unit) {
    var pendingManager by remember { mutableStateOf<Manager?>(null) }
    val salaryDue = EconomyEngine.managerSalaryDue(save.gameDay, save.managerLastPaidGameDay)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("مدیریت مربیان", color = Mint, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.End) {
                Text("سرمربی فعلی", color = Muted, fontSize = 12.sp)
                Text(save.managerName, color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("حقوق هر 3 روز: €" + "%,d".format(save.managerSalary), color = Muted, fontSize = 12.sp)
                Text(
                    if (salaryDue) "حقوق سررسید شده است" else "روزهای باقی‌مانده تا پرداخت: " + (3 - (save.gameDay - save.managerLastPaidGameDay)),
                    color = if (salaryDue) Color(0xFFFFB45C) else Mint,
                    fontSize = 12.sp
                )
                if (salaryDue) {
                    Button(
                        onClick = {
                            if (save.budget >= save.managerSalary) {
                                onSaveChange(save.copy(
                                    budget = save.budget - save.managerSalary,
                                    managerLastPaidGameDay = save.gameDay
                                ))
                            }
                        },
                        enabled = save.budget >= save.managerSalary,
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
                    ) { Text("پرداخت حقوق") }
                    if (save.budget < save.managerSalary) {
                        Text("بودجه برای پرداخت حقوق کافی نیست.", color = Color(0xFFFFB45C), fontSize = 12.sp)
                    }
                }
            }
        }
        Text("مربیان قابل خرید", color = Mint, fontWeight = FontWeight.Bold)
        ManagerCatalog.available.forEach { manager ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { pendingManager = manager },
                        enabled = save.budget >= manager.transferPrice,
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
                    ) { Text("خرید") }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(manager.name, color = White, fontWeight = FontWeight.Bold)
                        Text("OVR " + manager.overall, color = Mint, fontSize = 12.sp)
                        Text("قیمت: €" + "%,d".format(manager.transferPrice), color = Muted, fontSize = 11.sp)
                        Text("حقوق هر 3 روز: €" + "%,d".format(manager.salaryEveryThreeDays), color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
    pendingManager?.let { manager ->
        AlertDialog(
            onDismissRequest = { pendingManager = null },
            title = { Text("تأیید خرید مربی") },
            text = { Text("آیا می‌خواهی " + manager.name + " را با قیمت €" + "%,d".format(manager.transferPrice) + " استخدام کنی؟") },
            confirmButton = {
                TextButton(onClick = {
                    if (save.budget >= manager.transferPrice) {
                        onSaveChange(save.copy(
                            managerName = manager.name,
                            managerSalary = manager.salaryEveryThreeDays,
                            managerLastPaidGameDay = save.gameDay,
                            budget = ManagerEngine.buy(manager, save.budget)
                        ))
                    }
                    pendingManager = null
                }) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { pendingManager = null }) { Text("انصراف") }
            }
        )
    }
}


@Composable
private fun FinanceScreen(save: GameSave, onSaveChange: (GameSave) -> Unit) {
    val activeSponsor = save.sponsorContract?.takeIf { it.seasonNumber == save.season }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("امور مالی", color = Mint, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.End) {
                Text("موجودی باشگاه", color = Muted, fontSize = 12.sp)
                Text("€" + "%,d".format(save.budget), color = Mint, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("فصل " + save.season, color = Muted, fontSize = 11.sp)
            }
        }
        Text("قرارداد اسپانسر", color = Mint, fontWeight = FontWeight.Bold)
        if (activeSponsor != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(13.dp), horizontalAlignment = Alignment.End) {
                    Text(activeSponsor.sponsorName, color = White, fontWeight = FontWeight.Bold)
                    Text("درآمد ثابت این فصل: €" + "%,d".format(activeSponsor.payment), color = Mint, fontSize = 12.sp)
                }
            }
        } else {
            Text("برای این فصل هنوز اسپانسری انتخاب نشده است.", color = Muted, fontSize = 12.sp)
            SponsorCatalog.offers.forEach { offer ->
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val contract = SponsorEngine.sign(offer, save.season, save.sponsorContract)
                                val entry = FinanceEntry(
                                    id = "sponsor-" + offer.name.lowercase().replace(" ", "-") + "-" + save.season,
                                    gameDay = save.gameDay,
                                    category = "SPONSOR",
                                    description = "Season sponsor: " + offer.name,
                                    amount = offer.seasonPayment
                                )
                                onSaveChange(save.copy(
                                    sponsorContract = contract,
                                    budget = save.budget + offer.seasonPayment,
                                    financeLedger = save.financeLedger + entry
                                ))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
                        ) { Text("انتخاب") }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(offer.name, color = White, fontWeight = FontWeight.Bold)
                            Text("€" + "%,d".format(offer.seasonPayment) + " / فصل", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Divider(color = PanelLight)
        Text("تاریخچه مالی", color = Mint, fontWeight = FontWeight.Bold)
        if (save.financeLedger.isEmpty()) {
            Text("هنوز تراکنشی ثبت نشده است.", color = Muted, fontSize = 12.sp)
        } else {
            save.financeLedger.sortedByDescending { it.gameDay }.take(30).forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text((if (entry.amount >= 0) "+" else "") + "€" + "%,d".format(entry.amount), color = if (entry.amount >= 0) Mint else Color(0xFFFF8A80), fontSize = 12.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(entry.description, color = White, fontSize = 12.sp)
                        Text("روز " + entry.gameDay + " • " + entry.category, color = Muted, fontSize = 10.sp)
                    }
                }
                Divider(color = PanelLight)
            }
        }
    }
}
