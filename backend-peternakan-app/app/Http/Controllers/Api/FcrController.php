<?php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\FcrService;
use Illuminate\Database\Eloquent\ModelNotFoundException;

class FcrController extends Controller
{
    protected $fcrService;

    public function __construct(FcrService $fcrService)
    {
        $this->fcrService = $fcrService;
    }

    public function getCageFcr($cageId)
    {
        try {
            $data = $this->fcrService->calculateCageFcr($cageId);

            return response()->json([
                'status' => 'success',
                'data'   => $data,
            ], 200);
        } catch (ModelNotFoundException $e) {
            return response()->json(['message' => 'Kandang tidak ditemukan'], 404);
        }
    }
}