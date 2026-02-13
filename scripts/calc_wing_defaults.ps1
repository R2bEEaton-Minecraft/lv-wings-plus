param(
    [Parameter(Mandatory = $true)]
    [string]$TexturePath,
    [ValidateSet("avian", "insectoid")]
    [string]$Profile = "avian",
    [switch]$Json
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

function New-Rect([int]$X, [int]$Y, [int]$W, [int]$H) {
    return @{
        x = $X
        y = $Y
        w = $W
        h = $H
    }
}

function Clamp-Byte([double]$Value) {
    if ($Value -lt 0) { return 0 }
    if ($Value -gt 255) { return 255 }
    return [int][Math]::Round($Value)
}

function Get-AverageRgb($Bitmap, $Rects) {
    $sumR = 0.0
    $sumG = 0.0
    $sumB = 0.0
    $count = 0.0

    foreach ($rect in $Rects) {
        $x0 = [int]$rect.x
        $y0 = [int]$rect.y
        $w = [int]$rect.w
        $h = [int]$rect.h

        for ($y = $y0; $y -lt ($y0 + $h); $y++) {
            for ($x = $x0; $x -lt ($x0 + $w); $x++) {
                if ($x -lt 0 -or $y -lt 0 -or $x -ge $Bitmap.Width -or $y -ge $Bitmap.Height) {
                    continue
                }

                $c = $Bitmap.GetPixel($x, $y)
                if ($c.A -eq 0) {
                    continue
                }

                $sumR += $c.R
                $sumG += $c.G
                $sumB += $c.B
                $count += 1.0
            }
        }
    }

    if ($count -eq 0.0) {
        return @{ r = 255; g = 255; b = 255 }
    }

    return @{
        r = Clamp-Byte ($sumR / $count)
        g = Clamp-Byte ($sumG / $count)
        b = Clamp-Byte ($sumB / $count)
    }
}

function To-Hex($Rgb) {
    return ("0x{0:X2}{1:X2}{2:X2}" -f $Rgb.r, $Rgb.g, $Rgb.b)
}

$profiles = @{
    avian = @{
        leftStem = @(
            (New-Rect 0 28 5 3),
            (New-Rect 0 0 7 3),
            (New-Rect 22 0 9 3),
            (New-Rect 22 0 5 2)
        )
        rightStem = @(
            (New-Rect 0 34 5 3),
            (New-Rect 0 7 7 3),
            (New-Rect 22 6 9 3),
            (New-Rect 22 0 5 2)
        )
        leftFeathers = @(
            (New-Rect 6 40 6 8),
            (New-Rect 10 14 10 14),
            (New-Rect 31 14 11 12),
            (New-Rect 53 14 11 11)
        )
        rightFeathers = @(
            (New-Rect 0 40 6 8),
            (New-Rect 0 14 10 14),
            (New-Rect 20 14 11 12),
            (New-Rect 42 14 11 11)
        )
    }
    insectoid = @{
        leftStem = @((New-Rect 0 0 19 24))
        rightStem = @((New-Rect 0 24 19 24))
        leftFeathers = @((New-Rect 0 0 19 24))
        rightFeathers = @((New-Rect 0 24 19 24))
    }
}

$resolvedTexture = (Resolve-Path $TexturePath).Path
$profileRects = $profiles[$Profile]

$bitmap = New-Object System.Drawing.Bitmap($resolvedTexture)
try {
    $leftStem = Get-AverageRgb $bitmap $profileRects.leftStem
    $rightStem = Get-AverageRgb $bitmap $profileRects.rightStem
    $leftFeathers = Get-AverageRgb $bitmap $profileRects.leftFeathers
    $rightFeathers = Get-AverageRgb $bitmap $profileRects.rightFeathers
} finally {
    $bitmap.Dispose()
}

$result = [ordered]@{
    profile = $Profile
    texture = $resolvedTexture
    leftStem = To-Hex $leftStem
    rightStem = To-Hex $rightStem
    leftFeathers = To-Hex $leftFeathers
    rightFeathers = To-Hex $rightFeathers
    java = "new WingsArmorItem.PartColors($((To-Hex $leftStem)), $((To-Hex $rightStem)), $((To-Hex $leftFeathers)), $((To-Hex $rightFeathers)))"
}

if ($Json) {
    $result | ConvertTo-Json -Depth 4
    exit 0
}

Write-Output ("Profile      : {0}" -f $result.profile)
Write-Output ("Texture      : {0}" -f $result.texture)
Write-Output ("leftStem     : {0}" -f $result.leftStem)
Write-Output ("rightStem    : {0}" -f $result.rightStem)
Write-Output ("leftFeathers : {0}" -f $result.leftFeathers)
Write-Output ("rightFeathers: {0}" -f $result.rightFeathers)
Write-Output ("Java         : {0}" -f $result.java)
