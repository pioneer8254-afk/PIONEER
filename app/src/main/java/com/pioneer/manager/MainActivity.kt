package com.pioneer.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
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
    Column(modifier = Modifier.fillMaxSize().padding(top = 18.dp)) {
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
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Navy)
                    ) { Text(item.second) }
                } else {
                    OutlinedButton(onClick = { onTabChange(item.first) }) { Text(item.second, color = Muted) }
                }
            }
        }
        Divider(color = PanelLight)
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
            "finance" -> InfoScreen("امور مالی", "درآمد مسابقه، اسپانسر، خرید و فروش و حقوق مربی در این بخش ثبت می‌شود.")
            "news" -> InfoScreen("اخبار باشگاه", "رویدادهای مهم باشگاه و اعلان‌ها در این بخش نمایش داده می‌شوند.")
            "settings" -> SettingsScreen(save) { newName ->
                onSaveChange(save.copy(clubName = newName.trim()))
            }
            else -> HomeScreen(save)
        }
    }
}

@Composable
private fun HomeScreen(save: GameSave) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.End) {
                Text("باشگاه من", color = Muted, fontSize = 13.sp)
                Text(save.clubName, color = White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("Premier League  •  2026/27", color = Mint, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Text("سرمربی: " + save.managerName, color = White, fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Text("بودجه باشگاه", color = Muted, fontSize = 12.sp)
                Text("€" + "%,d".format(save.budget), color = Mint, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("روز فصل", save.gameDay.toString(), Modifier.weight(1f))
            StatCard("فصل", save.season.toString(), Modifier.weight(1f))
            StatCard("بازیکنان", "—", Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = PanelLight), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.End) {
                Text("وضعیت توسعه", color = Mint, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "ساختار اولیه برنامه و ذخیره خودکار فعال است. اطلاعات کامل بازیکنان، مربیان، لیگ‌های پایین‌تر، تقویم و موتور مسابقه در گام‌های بعدی تکمیل و آزمایش می‌شوند.",
                    color = White, fontSize = 13.sp, lineHeight = 21.sp
                )
            }
        }
        Text("ویژگی‌های پایه", color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text("• ذخیره خودکار تک‌باشگاه", color = Muted, fontSize = 13.sp)
        Text("• گذر روز بازی پس از هر 24 ساعت واقعی", color = Muted, fontSize = 13.sp)
        Text("• رابط فارسی و نام انگلیسی باشگاه‌ها", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.End) {
            Text(title, color = Muted, fontSize = 11.sp)
            Text(value, color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.End) {
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
