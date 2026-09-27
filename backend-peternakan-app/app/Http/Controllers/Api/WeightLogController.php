<?php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\WeightLogService;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Http\Request;

class WeightLogController extends Controller
{
    protected $weightLogService;

    public function __construct(WeightLogService $weightLogService)
    {
        $this->weightLogService = $weightLogService;
    }

    public function storeByTag(Request $request, $tagNumber)
    {
        $validated = $request->validate([
            'weight' => 'required|numeric|min:1',
            'date'   => 'required|date',
        ]);
        try {
            $log = $this->weightLogService->recordWeightByTag(
                $tagNumber,
                $validated,
                $request->user()->id
            );

            return response()->json([
                'message' => 'Data penimbangan berhasil dicatat',
                'data'    => $log->load('livestock'),
            ], 201);
        } catch (ModelNotFoundException $e) {
            return response()->json(['message' => 'Sapi tidak ditemukan'], 404);
        }
    }
}   