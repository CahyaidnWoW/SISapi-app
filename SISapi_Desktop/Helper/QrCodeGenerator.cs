using QRCoder;
using System.IO;
using System.Windows.Media.Imaging;

namespace SISapi_Desktop.Helpers
{
    public static class QrCodeGenerator
    {
        public static BitmapImage GenerateBitmap(string text)
        {
            using (var qrGenerator = new QRCodeGenerator())
            {
                using (var qrCodeData = qrGenerator.CreateQrCode(text, QRCodeGenerator.ECCLevel.Q))
                {
                    var pngQrCode = new PngByteQRCode(qrCodeData);
                    byte[] qrBytes = pngQrCode.GetGraphic(15);

                    var bitmap = new BitmapImage();
                    using (var stream = new MemoryStream(qrBytes))
                    {
                        bitmap.BeginInit();
                        bitmap.CacheOption = BitmapCacheOption.OnLoad;
                        bitmap.StreamSource = stream;
                        bitmap.EndInit();
                    }
                    bitmap.Freeze();

                    return bitmap;
                }
            }
        }

        public static void SaveAsPng(string text, string filePath)
        {
            using (var qrGenerator = new QRCodeGenerator())
            {
                using (var qrCodeData = qrGenerator.CreateQrCode(text, QRCodeGenerator.ECCLevel.Q))
                {
                    var pngQrCode = new PngByteQRCode(qrCodeData);
                    byte[] qrBytes = pngQrCode.GetGraphic(15);

                    File.WriteAllBytes(filePath, qrBytes);
                }
            }
        }
    }
}