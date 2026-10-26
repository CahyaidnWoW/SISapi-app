<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ActivityLog;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ActivityLogController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $limit = $request->get('limit', 15);

        $logs = ActivityLog::with('user:id,name,role')
            ->orderBy('id', 'desc')
            ->limit($limit)
            ->get();

        return response()->json([
            'status' => 'success',
            'data'   => $logs
        ]);
    }
}
