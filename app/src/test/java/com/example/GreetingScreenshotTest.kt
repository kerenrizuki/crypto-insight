package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.CryptoToken
import com.example.ui.screens.TokenTableRow
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockToken = CryptoToken(
        id = "ethereum",
        name = "Ethereum",
        symbol = "ETH",
        sector = "L1/L2",
        price = 3450.0,
        marketCap = 414000000000.0,
        fullyDilutedValuation = 414000000000.0,
        tvl = 52400000000.0,
        annualizedRevenue = 2850000000.0,
        psRatio = 145.2,
        activeAddresses = 420000,
        volume24h = 16200000000.0,
        revenue24hChange = 4.2,
        description = "Ethereum is a decentralized, open-source blockchain",
        tokenomicsDetails = "EIP-1559 introduces a gas fee burning mechanism",
        historyPrices = listOf(3100.0, 3150.0, 3200.0),
        historyRevenue = listOf(2.1e6, 2.3e6, 2.85e6),
        historyTvl = listOf(48.2e9, 49.5e9, 52.4e9)
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        TokenTableRow(
          token = mockToken,
          isStarred = true,
          columnVisibility = mapOf("TVL" to true),
          onStarToggle = {},
          onClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
