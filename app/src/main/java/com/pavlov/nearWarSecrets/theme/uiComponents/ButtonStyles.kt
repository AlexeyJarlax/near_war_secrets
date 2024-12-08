package com.pavlov.nearWarSecrets.theme.uiComponents

import androidx.compose.animation.animateColorAsState
import com.pavlov.nearWarSecrets.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavlov.nearWarSecrets.theme.My5
import com.pavlov.nearWarSecrets.theme.My7
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.pavlov.nearWarSecrets.theme.My3

@Composable
fun CustomButtonOne(
    onClick: () -> Unit,
    text: String,
    icon: Any? = null, // Принимаем либо ImageVector, либо Int (ресурс)
    modifier: Modifier = Modifier,
    textColor: Color = My3,
    iconColor: Color = My3,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp,
    iconPadding: Int = 0, // в редких случаях (например окно логина кнопка Вход делаю корректировку иконки)
    pressedColor: Color = My7
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (enabled) Color.Transparent else pressedColor
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor
        ),
        modifier = modifier
            .padding(start = 0.dp, end = 0.dp, bottom = 0.dp, top = 0.dp)
            .height(54.dp)
            .wrapContentWidth(),
        enabled = enabled,
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (icon != null) {
            when (icon) {
                is ImageVector -> Icon(
                    modifier = modifier.padding(bottom = iconPadding.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) iconColor else Color.Gray
                )
                is Int -> Icon(
                    modifier = modifier.padding(bottom = iconPadding.dp),
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = if (enabled) iconColor else Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = FontFamily.Default,
            color = if (enabled) textColor else Color.Gray
        )
    }
}

/** Пример использования:

Spacer(modifier = Modifier.height(8.dp))

var password by remember { mutableStateOf("") }

CustomButtonOne(
onClick = { viewModel.onPasswordEntered(password) },
text = stringResource(R.string.enter),
textColor = if (password.isEmpty()) My5 else My7,
iconColor = if (password.isEmpty()) My5 else My7,
icon = R.drawable.login_30dp или Icons.Default.InsertPhoto
)

или
CustomButtonOne(
onClick = { },
text = if (isPreviewVisible)
context.getString(R.string.cam_bat_hide)
else
context.getString(R.string.cam_bat_start),
icon = Icons.Default.InsertPhoto
)

 не желательно передавать modifier
 */

@Composable
fun CustomButtonTwo(
    onClick: () -> Unit,
    text: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
    textColor: Color = colorResource(id = R.color.my_prime_day),
    iconColor: Color = colorResource(id = R.color.my_prime_day)
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = textColor
        ),
        modifier = modifier
            .padding(end = 6.dp, bottom = 6.dp)
            .background(Color.Transparent)
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
//        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(1.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            letterSpacing = 0.0.sp,
            fontFamily = FontFamily.Default,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconColor
        )
    }
}

/**
CustomButtonTwo(
onClick = { viewModel.seePrivacyPolicy() },
text = stringResource(R.string.pp),
iconResId = R.drawable.description_30dp,
modifier = Modifier.fillMaxWidth()
)
 */

@Composable
fun CustomButtonElegantWithAStrokeDark(
    onClick: () -> Unit,
    text: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
    textColor: Color = colorResource(id = R.color.my_prime_day),
    iconColor: Color = colorResource(id = R.color.my_prime_day)
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = textColor
        ),
        modifier = modifier
            .padding(end = 10.dp, bottom = 10.dp)
            .background(Color.Transparent)
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
        shape = RoundedCornerShape(2.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            letterSpacing = 0.0.sp,
            fontFamily = FontFamily.Default,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
CustomButtonElegantWithAStrokeLight(
onClick = {
viewModel.generateRandomName()
tempName = viewModel.currentName
},
text = stringResource(R.string.generate_random_name),
iconResId = R.drawable.dice_30dp,
modifier = Modifier
.align(Alignment.CenterHorizontally)
.fillMaxWidth()
)
 */

@Composable
fun CustomButtonElegantWithAStrokeLight(
    onClick: () -> Unit,
    text: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    iconColor: Color = Color.White,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = textColor
        ),
        modifier = modifier
            .padding(end = 10.dp, bottom = 10.dp)
            .background(Color.Transparent)
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
        shape = RoundedCornerShape(2.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            letterSpacing = 0.0.sp,
            fontFamily = FontFamily.Default,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CustomYandexSignInButton(
    onClick: () -> Unit,
    text: String,
    iconResId: Int,
    textColor: Color
) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        modifier = Modifier
            .wrapContentWidth()
            .padding(2.dp)
            .shadow(2.dp, shape = RoundedCornerShape(2.dp)),

        shape = RoundedCornerShape(2.dp),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp)
        ) {

            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                letterSpacing = (0.0).sp,
                modifier = Modifier.padding(start = 8.dp)
            )

            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .padding(vertical = 0.dp)
                    .size(80.dp, 30.dp)
            )
        }
    }
}