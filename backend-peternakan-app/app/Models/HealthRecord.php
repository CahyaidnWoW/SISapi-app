<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class HealthRecord extends Model
{
    protected $guarded = ['id'];

    public function livestock() {
        return $this->belongsTo(Livestock::class);
    }

    public function handler() {
        return $this->belongsTo(User::class, 'handled_by');
    }
}
