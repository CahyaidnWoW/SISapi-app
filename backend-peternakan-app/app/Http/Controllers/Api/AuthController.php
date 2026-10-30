<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    /**
     * Handle user login and return Sanctum Token.
     */
        public function login(Request $request)
    {
        $request->validate([
            'email'    => 'required|email',
            'password' => 'required|string',
        ]);

        $user = User::where('email', $request->email)->first();

        if (! $user || ! Hash::check($request->password, $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['Kredensial yang diberikan tidak cocok dengan data kami.'],
            ]);
        }

        $deviceType = $request->input('device_type');
        if (!$deviceType) {
            $ua = strtolower($request->userAgent() ?? '');
            $deviceType = (str_contains($ua, 'okhttp') || str_contains($ua, 'android')) ? 'mobile' : 'desktop';
        }

        $tokenName = ($deviceType === 'mobile') ? 'mobile_auth_token' : 'desktop_auth_token';

        if ($deviceType === 'mobile' && $user->tokens()->where('name', 'mobile_auth_token')->exists()) {
            return response()->json([
                'message' => 'Akun ini sedang aktif di HP lain. Silakan logout terlebih dahulu dari HP tersebut.'
            ], 403);
        }

        if ($deviceType === 'desktop') {
            $user->tokens()->where('name', 'desktop_auth_token')->delete();
        }

        $token = $user->createToken($tokenName)->plainTextToken;

        return response()->json([
            'message'      => 'Login berhasil',
            'access_token' => $token,
            'token_type'   => 'Bearer',
            'user'         => [
                'id'    => $user->id,
                'name'  => $user->name,
                'email' => $user->email,
                'role'  => $user->role,
                'phone' => $user->phone,
            ],
        ], 200);
    }
    /**
     * Get authenticated user profile.
     */
    public function me(Request $request)
    {
        return response()->json([
            'message' => 'Profile retrieved successfully',
            'user'    => $request->user(),
        ], 200);
    }
    
    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'message' => 'Logout berhasil, token telah dihapus',
        ], 200);
    }
}
