package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.ui.theme.LocalExtendedColors

@Composable
fun AdaptiveContentBox(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
    Box(modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth()) { content() }
  }
}

@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  accentBorderColor: Color? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val ext = LocalExtendedColors.current
  val shape = RoundedCornerShape(20.dp)
  val borderColor = accentBorderColor ?: ext.subtleBorder.copy(alpha = 0.55f)
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    shape = shape,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Column(modifier = Modifier.padding(16.dp), content = content)
  }
}

@Composable
fun StatBadgePill(
  icon: ImageVector,
  value: String,
  label: String,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50.dp),
    color = tint.copy(alpha = 0.14f),
    border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = tint,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = value,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = LocalExtendedColors.current.mutedText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  actionTestTag: String? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onBackground,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    if (actionLabel != null && onAction != null) {
      TextButton(
        onClick = onAction,
        modifier =
          Modifier.minimumInteractiveComponentSize()
            .then(if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier),
      ) {
        Text(
          text = actionLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Composable
fun EmptyStateCard(
  icon: ImageVector,
  title: String,
  body: String,
  modifier: Modifier = Modifier,
) {
  val ext = LocalExtendedColors.current
  GlassCard(modifier = modifier) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier =
          Modifier.size(56.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                  MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                )
              )
            ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp),
        )
      }
      Spacer(Modifier.height(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = ext.mutedText,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
fun GradientProgressBar(
  progress: Float,
  modifier: Modifier = Modifier,
  heightDp: Int = 8,
) {
  val clamped = progress.coerceIn(0f, 1f)
  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val gradient =
    Brush.horizontalGradient(
      colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(heightDp.dp)
        .clip(RoundedCornerShape(50))
        .background(trackColor)
  ) {
    if (clamped > 0f) {
      Box(
        modifier =
          Modifier.fillMaxWidth(clamped)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(50))
            .background(gradient)
      )
    }
  }
}

@Composable
fun localizedSubjectName(nameEn: String): String {
  val lang = LocalAppLanguage.current
  if (lang != AppLanguage.BENGALI) return nameEn
  return when (nameEn) {
    "General" -> "সাধারণ"
    "Physics" -> "পদার্থবিজ্ঞান"
    "Chemistry" -> "রসায়ন"
    "General Math" -> "সাধারণ গণিত"
    "Higher Math" -> "উচ্চতর গণিত"
    "Biology" -> "জীববিজ্ঞান"
    "English" -> "ইংরেজি"
    "Bangla" -> "বাংলা"
    "ICT" -> "তথ্য ও যোগাযোগ প্রযুক্তি"
    else -> nameEn
  }
}
