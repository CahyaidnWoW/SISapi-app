<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use App\Models\ActivityLog;

class LogActivity
{
    public function handle(Request $request, Closure $next)
    {
        $response = $next($request);

        if ($response->isSuccessful() && in_array($request->method(), ['POST', 'PUT', 'PATCH', 'DELETE'])) {
            $user = $request->user();
            if (!$user) return $response;

            if ($request->is('api/login') || $request->is('api/logout')) {
                return $response;
            }

            $action = match ($request->method()) {
                'POST'   => 'CREATE',
                'PUT', 'PATCH' => 'UPDATE',
                'DELETE' => 'DELETE',
                default  => 'UNKNOWN'
            };

            $moduleMap = [
                'cages'         => 'Kandang',
                'livestocks'    => 'Ternak',
                'feeds'         => 'Pakan',
                'health-records'=> 'Rekam Medis',
                'weight-logs'   => 'Timbangan',
                'breeds'        => 'Ras Sapi',
            ];

            $segment = $request->segment(2);
            $module = $moduleMap[$segment] ?? ucfirst($segment ?? 'Sistem');

            $actionWord = match ($action) {
                'CREATE' => 'menambahkan data',
                'UPDATE' => 'memperbarui data',
                'DELETE' => 'menghapus data',
                default  => 'mengubah data'
            };

            $platform = $request->header('X-Client-Platform')
                        ?? ($user->role === 'worker' ? 'mobile' : 'desktop');

            ActivityLog::create([
                'user_id'     => $user->id,
                'action'      => $action,
                'module'      => $module,
                'description' => "{$user->name} {$actionWord} pada modul {$module}",
                'platform'    => strtolower($platform),
                'ip_address'  => $request->ip(),
            ]);
        }

        return $response;
    }
}
