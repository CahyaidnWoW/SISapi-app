<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class WeightLog extends Model
{
    protected $guarded = ['id'];

    public function livestock() {
        return $this->belongsTo(Livestock::class);
    }

    public function worker() {
        return $this->belongsTo(User::class, 'worker_id');
    }
}    //
    
