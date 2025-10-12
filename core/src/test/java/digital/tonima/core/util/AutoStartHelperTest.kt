package digital.tonima.core.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AutoStartHelperTest {

    private lateinit var context: Context
    private lateinit var packageManager: PackageManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        every { context.packageManager } returns packageManager
        every { context.packageName } returns "digital.tonima.kairos"
        every { context.getString(any()) } returns "Open settings"

        every { packageManager.resolveActivity(any<Intent>(), any<Int>()) } returns null

        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<String>(), any()) } returns toast
    }

    @Test
    fun `openAutostartSettings falls back to app settings and shows toast when no OEM activity`() {
        openAutostartSettings(context)

        verify { Toast.makeText(context, any<String>(), Toast.LENGTH_LONG) }

        verify(exactly = 1) { context.startActivity(any<Intent>()) }
    }
}
