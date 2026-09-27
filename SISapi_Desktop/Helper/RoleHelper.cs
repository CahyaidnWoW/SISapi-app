using SISapi.Desktop.Helper;

namespace SISapi_Desktop.Helpers
{
    public static class RoleHelper
    {
        public static bool IsManager => SessionManager.CurrentUser?.Role == "manager";
        public static bool IsVet => SessionManager.CurrentUser?.Role == "vet";
        public static bool IsWorker => SessionManager.CurrentUser?.Role == "worker";
    }
}